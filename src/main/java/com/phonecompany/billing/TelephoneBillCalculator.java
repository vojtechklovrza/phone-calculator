package com.phonecompany.billing;

import java.io.IOException;
import java.math.BigDecimal;

public interface TelephoneBillCalculator {

    BigDecimal calculate(String phoneLog) throws IOException;

}
