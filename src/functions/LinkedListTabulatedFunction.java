package functions;

public class LinkedListTabulatedFunction implements TabulatedFunction {

    // Внутренний класс для узла списка
    private static class FunctionNode {
        FunctionPoint point;
        FunctionNode prev;
        FunctionNode next;

        FunctionNode(FunctionPoint point) {
            this.point = point;
            this.prev = null;
            this.next = null;
        }
    }

    private FunctionNode head; // Голова списка (не содержит данных)
    private int pointsCount;
    private FunctionNode lastAccessedNode; // Кэш для оптимизации доступа
    private int lastAccessedIndex;

    private static final double EPSILON = 1e-10;

    public LinkedListTabulatedFunction(double leftX, double rightX, int pointsCount) {
        if (leftX >= rightX) {
            throw new IllegalArgumentException("Левая граница должна быть меньше правой");
        }
        if (pointsCount < 2) {
            throw new IllegalArgumentException("Количество точек должно быть не менее 2");
        }

        // Создаем голову списка
        head = new FunctionNode(null);
        head.next = head;
        head.prev = head;
        this.pointsCount = 0;

        double step = (rightX - leftX) / (pointsCount - 1);
        for (int i = 0; i < pointsCount; i++) {
            double x = leftX + i * step;
            addNodeToTail(new FunctionPoint(x, 0));
        }
    }

    public LinkedListTabulatedFunction(double leftX, double rightX, double[] values) {
        this(leftX, rightX, values.length);
        FunctionNode current = head.next;
        for (int i = 0; i < values.length; i++) {
            current.point.setY(values[i]);
            current = current.next;
        }
    }

    // Вспомогательные методы для работы со списком

    private FunctionNode getNodeByIndex(int index) {
        if (index < 0 || index >= pointsCount) {
            throw new FunctionPointIndexOutOfBoundsException(index);
        }

        // Оптимизация: если обращаемся к тому же или соседнему узлу
        if (lastAccessedNode != null && lastAccessedIndex != -1) {
            if (index == lastAccessedIndex) {
                return lastAccessedNode;
            } else if (index == lastAccessedIndex + 1 && index < pointsCount) {
                lastAccessedNode = lastAccessedNode.next;
                lastAccessedIndex++;
                return lastAccessedNode;
            } else if (index == lastAccessedIndex - 1 && index >= 0) {
                lastAccessedNode = lastAccessedNode.prev;
                lastAccessedIndex--;
                return lastAccessedNode;
            }
        }

        // Линейный поиск
        FunctionNode current;
        if (index < pointsCount / 2) {
            // Идем от начала
            current = head.next;
            for (int i = 0; i < index; i++) {
                current = current.next;
            }
        } else {
            // Идем от конца
            current = head.prev;
            for (int i = pointsCount - 1; i > index; i--) {
                current = current.prev;
            }
        }

        lastAccessedNode = current;
        lastAccessedIndex = index;
        return current;
    }

    private FunctionNode addNodeToTail(FunctionPoint point) {
        FunctionNode newNode = new FunctionNode(new FunctionPoint(point));
        FunctionNode tail = head.prev;

        newNode.prev = tail;
        newNode.next = head;
        tail.next = newNode;
        head.prev = newNode;

        pointsCount++;
        lastAccessedNode = newNode;
        lastAccessedIndex = pointsCount - 1;

        return newNode;
    }

    private FunctionNode addNodeByIndex(int index, FunctionPoint point) throws InappropriateFunctionPointException {
        if (index < 0 || index > pointsCount) {
            throw new FunctionPointIndexOutOfBoundsException(index);
        }

        // Проверка на существование точки с таким X
        if (index > 0) {
            FunctionNode prevNode = getNodeByIndex(index - 1);
            if (doubleEquals(prevNode.point.getX(), point.getX())) {
                throw new InappropriateFunctionPointException("Точка с таким X уже существует");
            }
        }
        if (index < pointsCount) {
            FunctionNode nextNode = getNodeByIndex(index);
            if (doubleEquals(nextNode.point.getX(), point.getX())) {
                throw new InappropriateFunctionPointException("Точка с таким X уже существует");
            }
        }

        FunctionNode newNode = new FunctionNode(new FunctionPoint(point));

        if (index == pointsCount) {
            return addNodeToTail(point);
        }

        FunctionNode nextNode = getNodeByIndex(index);
        FunctionNode prevNode = nextNode.prev;

        newNode.prev = prevNode;
        newNode.next = nextNode;
        prevNode.next = newNode;
        nextNode.prev = newNode;

        pointsCount++;
        lastAccessedNode = newNode;
        lastAccessedIndex = index;

        return newNode;
    }

    private FunctionNode deleteNodeByIndex(int index) {
        if (index < 0 || index >= pointsCount) {
            throw new FunctionPointIndexOutOfBoundsException(index);
        }
        if (pointsCount < 3) {
            throw new IllegalStateException("Нельзя удалить точку: должно остаться минимум 2 точки");
        }

        FunctionNode nodeToDelete = getNodeByIndex(index);
        FunctionNode prevNode = nodeToDelete.prev;
        FunctionNode nextNode = nodeToDelete.next;

        prevNode.next = nextNode;
        nextNode.prev = prevNode;

        pointsCount--;

        // Сбрасываем кэш, если удалили его элемент
        if (lastAccessedNode == nodeToDelete) {
            lastAccessedNode = null;
            lastAccessedIndex = -1;
        } else if (lastAccessedIndex > index) {
            lastAccessedIndex--;
        }

        return nodeToDelete;
    }

    // Методы интерфейса TabulatedFunction

    public double getLeftDomainBorder() {
        return head.next.point.getX();
    }

    public double getRightDomainBorder() {
        return head.prev.point.getX();
    }

    public double getFunctionValue(double x) {
        if (x < getLeftDomainBorder() || x > getRightDomainBorder()) {
            return Double.NaN;
        }

        // Поиск интервала
        FunctionNode current = head.next;
        while (current != head && current.next != head && current.next.point.getX() < x) {
            current = current.next;
        }

        if (doubleEquals(current.point.getX(), x)) {
            return current.point.getY();
        }
        if (current.next != head && doubleEquals(current.next.point.getX(), x)) {
            return current.next.point.getY();
        }

        // Линейная интерполяция
        double x1 = current.point.getX();
        double y1 = current.point.getY();
        double x2 = current.next.point.getX();
        double y2 = current.next.point.getY();

        return y1 + (y2 - y1) * (x - x1) / (x2 - x1);
    }

    public int getPointsCount() {
        return pointsCount;
    }

    public FunctionPoint getPoint(int index) {
        FunctionNode node = getNodeByIndex(index);
        return new FunctionPoint(node.point);
    }

    public void setPoint(int index, FunctionPoint point) throws InappropriateFunctionPointException {
        FunctionNode node = getNodeByIndex(index);

        double newX = point.getX();
        if ((index > 0 && newX <= getNodeByIndex(index - 1).point.getX()) ||
                (index < pointsCount - 1 && newX >= getNodeByIndex(index + 1).point.getX())) {
            throw new InappropriateFunctionPointException("Нарушение порядка точек по X");
        }

        node.point = new FunctionPoint(point);
    }

    public double getPointX(int index) {
        return getNodeByIndex(index).point.getX();
    }

    public void setPointX(int index, double x) throws InappropriateFunctionPointException {
        FunctionNode node = getNodeByIndex(index);

        if ((index > 0 && x <= getNodeByIndex(index - 1).point.getX()) ||
                (index < pointsCount - 1 && x >= getNodeByIndex(index + 1).point.getX())) {
            throw new InappropriateFunctionPointException("Нарушение порядка точек по X");
        }

        node.point.setX(x);
    }

    public double getPointY(int index) {
        return getNodeByIndex(index).point.getY();
    }

    public void setPointY(int index, double y) {
        getNodeByIndex(index).point.setY(y);
    }

    public void deletePoint(int index) {
        deleteNodeByIndex(index);
    }

    public void addPoint(FunctionPoint point) throws InappropriateFunctionPointException {
        // Поиск позиции для вставки
        int insertIndex = 0;
        FunctionNode current = head.next;

        while (current != head && current.point.getX() < point.getX()) {
            current = current.next;
            insertIndex++;
        }

        if (current != head && doubleEquals(current.point.getX(), point.getX())) {
            throw new InappropriateFunctionPointException("Точка с таким X уже существует");
        }

        addNodeByIndex(insertIndex, point);
    }

    private boolean doubleEquals(double a, double b) {
        return Math.abs(a - b) < EPSILON;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("LinkedListTabulatedFunction [pointsCount=").append(pointsCount).append("]\n");
        FunctionNode current = head.next;
        int i = 0;
        while (current != head) {
            sb.append(String.format("  [%d] x=%.3f, y=%.3f%n", i, current.point.getX(), current.point.getY()));
            current = current.next;
            i++;
        }
        return sb.toString();
    }
}