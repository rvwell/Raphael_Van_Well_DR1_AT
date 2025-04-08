package ex2;

import net.jqwik.api.*;

import static org.junit.jupiter.api.Assertions.*;

import net.jqwik.api.constraints.IntRange;
import net.jqwik.api.constraints.Positive;

import java.util.Arrays;

public class MathFunctionsPropertiesTest {

    @Property
    @Label("MultiplyByTwo: O resultado é sempre par")
    void multiplyByTwo(@ForAll int number) {
        int result = MathFunctions.MultiplyByTwo(number);

        assertEquals(0, result % 2, "Resultado de MultiplyByTwo deveria ser par. Número: " + number + ", Resultado: " + result);
    }

    @Property
    void generateMultiplicationTable(
            @ForAll int number,
            @ForAll @Positive @IntRange(max = 1000) int limit
    ) {
        int[] table = MathFunctions.GenerateMultiplicationTable(number, limit);
        assertEquals(limit, table.length, "Tamanho do array da tabuada incorreto.");

        if (number != 0) {
            for (int i = 0; i < table.length; i++) {
                long expectedValue = (long)number * (i + 1);
                int actualValueInTable = table[i];
                Assume.that((long)actualValueInTable == expectedValue);

                assertEquals(0, actualValueInTable % number,
                        "Elemento [" + i + "] = " + actualValueInTable + " deveria ser múltiplo de " + number + " (valor esperado: " + expectedValue + ")");
            }
        } else {
            for (int i = 0; i < table.length; i++) {
                assertEquals(0, table[i], "Elemento [" + i + "] da tabuada para number=0 deveria ser 0.");
            }
        }
    }

    @Property
    void generateMultiplicationTableSizeIsCorrect(
            @ForAll int number,
            @ForAll @Positive @IntRange(max = 1000) int limit
    ) {
        int[] table = MathFunctions.GenerateMultiplicationTable(number, limit);
        assertEquals(limit, table.length, "Tamanho do array da tabuada incorreto.");
    }

    @Property
    void isPrime(
            @ForAll @IntRange(min = 2, max = 65536) int a,
            @ForAll @IntRange(min = 2, max = 65536) int b
    ) {
        long compositeLong = (long) a * b;
        Assume.that(compositeLong <= Integer.MAX_VALUE);
        int compositeInt = (int) compositeLong;

        assertFalse(MathFunctions.IsPrime(compositeInt),
                "Número composto " + compositeInt + " (" + a + "*" + b + ") retornar false para IsPrime");
    }

    @Provide
    Arbitrary<int[]> nonEmptyIntArrays() {
        return Arbitraries.integers().between(-10000, 10000)
                .array(int[].class)
                .ofMinSize(1)
                .ofMaxSize(100);
    }

    @Property
    void calculateAverage(@ForAll("nonEmptyIntArrays") int[] numbers) {
        int min = Arrays.stream(numbers).min().getAsInt();
        int max = Arrays.stream(numbers).max().getAsInt();
        double average = MathFunctions.CalculateAverage(numbers);

        assertTrue(average >= min, "Média " + average + " >= mínimo " + min);
        assertTrue(average <= max, "Média " + average + " <= máximo " + max);
    }


}
