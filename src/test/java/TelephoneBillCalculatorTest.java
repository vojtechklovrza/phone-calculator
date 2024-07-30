import com.phonecompany.billing.TelephoneBillCalculator;
import com.phonecompany.billing.TelephoneBillCalculatorImpl;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.Paths;

import static org.junit.jupiter.api.Assertions.assertEquals;

class TelephoneBillCalculatorTest {

    @Test
    void test() throws IOException {
        TelephoneBillCalculator calculator = new TelephoneBillCalculatorImpl();
        String filePath = Paths.get("src/test/resources/phone_bill_01.csv").toString();
        BigDecimal amount = calculator.calculate(filePath);
        assertEquals(BigDecimal.valueOf(1.5), amount);

        filePath = Paths.get("src/test/resources/phone_bill_02.csv").toString();
        amount = calculator.calculate(filePath);
        assertEquals(BigDecimal.valueOf(6.20), amount);

        filePath = Paths.get("src/test/resources/phone_bill_03.csv").toString();
        amount = calculator.calculate(filePath);
        System.out.println("Amount: " + amount);
        assertEquals(BigDecimal.valueOf(1.50), amount);

        filePath = Paths.get("src/test/resources/phone_bill_04.csv").toString();
        amount = calculator.calculate(filePath);
        assertEquals(BigDecimal.valueOf(13.9), amount);
    }
}
