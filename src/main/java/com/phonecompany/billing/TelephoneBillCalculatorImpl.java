package com.phonecompany.billing;

import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

import static java.math.BigDecimal.ZERO;

public class TelephoneBillCalculatorImpl implements TelephoneBillCalculator {

    private static final BigDecimal PEAK_ZONE_PHONE_RATE = BigDecimal.valueOf(1.00);
    private static final BigDecimal OFF_PEAK_ZONE_PHONE_RATE = BigDecimal.valueOf(0.50);
    private static final BigDecimal REDUCED_PHONE_RATE = BigDecimal.valueOf(0.20);

    private static final int PEAK_START = 8;
    private static final int PEAK_END = 16;
    private static final int FREE_MINUTES = 5;

    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm:ss");

    @Override
    public BigDecimal calculate(String filePath) throws IOException {
        List<String> lines = Files.readAllLines(Paths.get(filePath));
        Map<String, BigDecimal> phoneBillMap = new HashMap<>(); // phone number -> total bill
        Map<String, Integer> callFrequencyMap = new HashMap<>(); // phone number -> call frequency

        for (String line : lines) { // parse the call log
            String[] details = line.split(","); // phone number, start date, end date
            String phoneNumber = details[0]; // phone number

            LocalDateTime startDate = LocalDateTime.parse(details[1], DATE_FORMAT); // start date
            LocalDateTime endDate = LocalDateTime.parse(details[2], DATE_FORMAT); // end date
            long durationInSeconds = Duration.between(startDate, endDate).getSeconds(); // call duration in seconds
            long durationInMinutes = (long) Math.ceil(durationInSeconds / 60.0); // call duration in minutes with rounding up
            BigDecimal callCost = calculateCallCost(startDate, durationInMinutes);

            phoneBillMap.put(phoneNumber, phoneBillMap.getOrDefault(phoneNumber, ZERO).add(callCost));
            callFrequencyMap.put(phoneNumber, callFrequencyMap.getOrDefault(phoneNumber, 0) + 1);
        }

        checkSinglePhoneNumber(callFrequencyMap, phoneBillMap); // one number is not included in promotion event

        return phoneBillMap.values().stream().reduce(ZERO, BigDecimal::add);
    }

    private BigDecimal calculateCallCost(LocalDateTime startDate, long durationInMinutes) {
        LocalDateTime currentMinute = startDate;
        BigDecimal totalCost = ZERO;

        for (long i = 0; i < durationInMinutes; i++) {
            int hourOfDay = currentMinute.getHour();
            if (hourOfDay >= PEAK_START && hourOfDay < PEAK_END) { // peak zone
                totalCost = totalCost.add(PEAK_ZONE_PHONE_RATE);
            } else { // off-peak zone
                totalCost = totalCost.add(OFF_PEAK_ZONE_PHONE_RATE);
            }
            currentMinute = currentMinute.plusMinutes(1);
        }

        if (durationInMinutes > FREE_MINUTES) {
            long reducedMinutes = durationInMinutes - FREE_MINUTES;
            totalCost = totalCost.subtract(PEAK_ZONE_PHONE_RATE.multiply(BigDecimal.valueOf(reducedMinutes)));
            totalCost = totalCost.add(REDUCED_PHONE_RATE.multiply(BigDecimal.valueOf(reducedMinutes)));
        }

        return totalCost;
    }

    private String getFreePhoneNumber(Map<String, Integer> callFrequencyMap) { // get the phone number with the highest call frequency
        return callFrequencyMap.entrySet().stream() // sort by call frequency and phone number
                .max((e1, e2) -> {
                    int freqCompare = e1.getValue().compareTo(e2.getValue()); // compare call frequencies
                    if (freqCompare == 0) { // if the call frequencies are equal, compare phone numbers
                        return e1.getKey().compareTo(e2.getKey());
                    }
                    return freqCompare; // return the comparison result
                })
                .map(Map.Entry::getKey)
                .orElse(null); // return the phone number with the highest call frequency
    }

    // if the bill contains just one phone number, the bill will be paid as normal not completely free, simply one number is not included in promotion event
    private void checkSinglePhoneNumber(Map<String, Integer> callFrequencyMap, Map<String, BigDecimal> phoneBillMap) {
        if (callFrequencyMap.size() > 1) {
            String freePhoneNumber = getFreePhoneNumber(callFrequencyMap);
            phoneBillMap.remove(freePhoneNumber);
        }
    }
}
