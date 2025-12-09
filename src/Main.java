import functions.*;

public class Main {
    public static void main(String[] args) {
        System.out.println("=== Тестирование лабораторной работы №3 ===\n");

        // Тест 1: ArrayTabulatedFunction с исключениями
        System.out.println("1. Тестирование ArrayTabulatedFunction:");
        testArrayFunction();

        System.out.println("\n" + "=".repeat(50) + "\n");

        // Тест 2: LinkedListTabulatedFunction с исключениями
        System.out.println("2. Тестирование LinkedListTabulatedFunction:");
        testLinkedListFunction();

        System.out.println("\n" + "=".repeat(50) + "\n");

        // Тест 3: Полиморфное использование через интерфейс
        System.out.println("3. Полиморфное использование:");
        testPolymorphism();

        System.out.println("\n=== Тестирование завершено ===");
    }

    private static void testArrayFunction() {
        try {
            // 1.1 Некорректное создание функции
            System.out.println("  1.1 Попытка создания с некорректными параметрами:");
            try {
                TabulatedFunction func = new ArrayTabulatedFunction(5, 3, 4);
            } catch (IllegalArgumentException e) {
                System.out.println("      Поймано исключение: " + e.getMessage());
            }

            try {
                TabulatedFunction func = new ArrayTabulatedFunction(0, 5, 1);
            } catch (IllegalArgumentException e) {
                System.out.println("      Поймано исключение: " + e.getMessage());
            }

            // 1.2 Корректное создание
            System.out.println("\n  1.2 Корректное создание функции:");
            TabulatedFunction func = new ArrayTabulatedFunction(0, 10, 5);
            System.out.println("      Функция создана. Точек: " + func.getPointsCount());

            // 1.3 Выход за границы индексов
            System.out.println("\n  1.3 Тест выхода за границы индексов:");
            try {
                func.getPoint(10);
            } catch (FunctionPointIndexOutOfBoundsException e) {
                System.out.println("      Поймано исключение: " + e.getMessage());
            }

            // 1.4 Некорректная установка точки
            System.out.println("\n  1.4 Тест некорректной установки точки:");
            try {
                func.setPoint(2, new FunctionPoint(8, 5));
            } catch (InappropriateFunctionPointException e) {
                System.out.println("      Поймано исключение: " + e.getMessage());
            }

            // 1.5 Некорректное добавление точки
            System.out.println("\n  1.5 Тест некорректного добавления точки:");
            try {
                func.addPoint(new FunctionPoint(2.5, 3));
                func.addPoint(new FunctionPoint(2.5, 4)); // Дублирование X
            } catch (InappropriateFunctionPointException e) {
                System.out.println("      Поймано исключение: " + e.getMessage());
            }

            // 1.6 Некорректное удаление точки
            System.out.println("\n  1.6 Тест некорректного удаления:");
            // Сначала добавим точки
            try {
                func.deletePoint(0);
                func.deletePoint(0);
                func.deletePoint(0); // Попытка удалить третью точку (останется 2)
            } catch (IllegalStateException e) {
                System.out.println("      Поймано исключение: " + e.getMessage());
            }

        } catch (Exception e) {
            System.out.println("Неожиданное исключение: " + e);
            e.printStackTrace();
        }
    }

    private static void testLinkedListFunction() {
        try {
            // 2.1 Корректное создание
            System.out.println("  2.1 Корректное создание функции:");
            TabulatedFunction func = new LinkedListTabulatedFunction(-5, 5, 6);
            System.out.println("      Функция создана. Точек: " + func.getPointsCount());

            // 2.2 Работа с точками
            System.out.println("\n  2.2 Работа с точками:");
            for (int i = 0; i < func.getPointsCount(); i++) {
                func.setPointY(i, i * i); // y = x^2
            }

            System.out.println("      Значения в точках:");
            for (int i = 0; i < func.getPointsCount(); i++) {
                System.out.printf("      f(%.1f) = %.1f%n", func.getPointX(i), func.getPointY(i));
            }

            // 2.3 Добавление и удаление точек
            System.out.println("\n  2.3 Добавление и удаление точек:");
            try {
                func.addPoint(new FunctionPoint(2.5, 6.25));
                System.out.println("      Точка (2.5, 6.25) добавлена");

                func.deletePoint(3);
                System.out.println("      Точка с индексом 3 удалена");
            } catch (Exception e) {
                System.out.println("      Исключение: " + e.getMessage());
            }

            // 2.4 Интерполяция
            System.out.println("\n  2.4 Тест интерполяции:");
            System.out.printf("      f(0.5) = %.2f%n", func.getFunctionValue(0.5));
            System.out.printf("      f(10) = %s%n", func.getFunctionValue(10)); // Вне области определения

        } catch (Exception e) {
            System.out.println("Неожиданное исключение: " + e);
            e.printStackTrace();
        }
    }

    private static void testPolymorphism() {
        System.out.println("  Работа через интерфейс TabulatedFunction:");

        // Массив функций для тестирования
        TabulatedFunction[] functions = new TabulatedFunction[2];

        // Создаем функции разных типов
        functions[0] = new ArrayTabulatedFunction(0, Math.PI, new double[]{0, 1, 0, -1, 0});
        functions[1] = new LinkedListTabulatedFunction(-2, 2, new double[]{4, 1, 0, 1, 4});

        String[] names = {"sin(x)", "x^2"};

        for (int i = 0; i < functions.length; i++) {
            System.out.println("\n  Функция " + names[i] + " (" +
                    functions[i].getClass().getSimpleName() + "):");
            System.out.println("    Область определения: [" +
                    functions[i].getLeftDomainBorder() + ", " +
                    functions[i].getRightDomainBorder() + "]");
            System.out.println("    Количество точек: " + functions[i].getPointsCount());

            // Вычисляем значения в нескольких точках
            double[] testX = {0.5, 1.0, 1.5};
            for (double x : testX) {
                double value = functions[i].getFunctionValue(x);
                System.out.printf("    f(%.1f) = ", x);
                if (Double.isNaN(value)) {
                    System.out.println("не определено");
                } else {
                    System.out.printf("%.3f%n", value);
                }
            }
        }
    }
}