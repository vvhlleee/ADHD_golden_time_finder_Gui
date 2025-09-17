package org.example.adhd_test_gui;

import java.time.LocalDateTime;

public class TestResult {
    private LocalDateTime testDateTime;
    private int taskScore;

    public TestResult(LocalDateTime testDateTime, int taskScore) {
        this.testDateTime = testDateTime;
        this.taskScore = taskScore;
    }

    public LocalDateTime getTestDateTime() {
        return testDateTime;
    }

    public int getTaskScore() {
        return taskScore;
    }

    // CSV 파일에 저장하기 위한 포맷
    public String toCsvString() {
        return testDateTime.toString() + "," + taskScore;
    }

    // CSV 파일에서 읽어오기 위한 포맷
    public static TestResult fromCsvString(String csvLine) {
        String[] parts = csvLine.split(",");
        LocalDateTime dateTime = LocalDateTime.parse(parts[0]);
        int score = Integer.parseInt(parts[1]);
        return new TestResult(dateTime, score);
    }
}