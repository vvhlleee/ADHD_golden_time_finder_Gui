package org.example.adhd_test_gui;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;
import javafx.application.Application;
public class Main{

    private static final String FILENAME = "math_results.csv";
    private static final Scanner scanner = new Scanner(System.in);

    public static void main(String[] args) {
        Application.launch(GuiApplication.class, args);
        while (true) {
            System.out.println("\n=== Golden Time Finder (Math Edition) ===");
            System.out.println("1. 집중력 테스트 시작 (30초 사칙연산 퀴즈)");
            System.out.println("2. 최종 결과 리포트 보기");
            System.out.println("3. 종료");
            System.out.print("메뉴를 선택하세요: ");

            String choice = scanner.nextLine();

            switch (choice) {
                case "1": // 테스트 시작
                    runTest();
                    break;
                case "2": // 데이터 분석
                    showAnalysisReport();
                    break;
                case "3": // 종료
                    System.out.println("프로그램을 종료합니다.");
                    return;
                default:
                    System.out.println("잘못된 입력입니다. 다시 선택해주세요.");
            }
        }
    }

    private static void runTest() {
        final int DURATION_SECONDS = 30; // 제한 시간 30초
        Random random = new Random();
        int score = 0;
        int unscore = 0;

        System.out.println("테스트 시작! 30초 동안 사칙연산 문제를 최대한 많이 풀어보세요.");
        long endTime = System.currentTimeMillis() + (DURATION_SECONDS * 1000L);

        while (System.currentTimeMillis() < endTime) {
            int num1, num2, answer;
            String problem;

            int operator = random.nextInt(4); // 0은 +, 1은 -, 2는 *, 3은 /

            switch (operator) {
                case 0: // 덧셈
                    num1 = random.nextInt(100) + 1; // 1 ~ 100
                    num2 = random.nextInt(100) + 1;
                    answer = num1 + num2;
                    problem = String.format("%d + %d", num1, num2);
                    break;
                case 1: // 뺄셈
                    num1 = random.nextInt(100) + 1;
                    num2 = random.nextInt(100) + 1;
                    int larger = Math.max(num1, num2);
                    int smaller = Math.min(num1, num2);
                    answer = larger - smaller;
                    problem = String.format("%d - %d", larger, smaller);
                    break;
                case 2: // 곱셈
                    num1 = random.nextInt(9) + 2; // 2 ~ 10
                    num2 = random.nextInt(9) + 2;
                    answer = num1 * num2;
                    problem = String.format("%d * %d", num1, num2);
                    break;
                case 3: // 나눗셈
                    int divisor = random.nextInt(9) + 2;   // 2 ~ 10
                    int quotient = random.nextInt(9) + 2;
                    int dividend = divisor * quotient;
                    answer = quotient;
                    problem = String.format("%d / %d", dividend, divisor);
                    break;
                default: // 비상상황
                    answer = 0;
                    problem = "Error";
                    break;
            }

            System.out.printf("문제: %s = ", problem);

            try {
                String userInput = scanner.nextLine();
                if (userInput.isEmpty()) continue;

                int userAnswer = Integer.parseInt(userInput);
                if (userAnswer == answer) {
                    score++;
                }
                else if(userAnswer != answer)
                {
                    unscore++;
                }
            } catch (NumberFormatException e) { // 잘못된 입력
                System.out.println("숫자만 입력해주세요.");
            }
        }
        int sum = score + unscore; // 총 문제 수
        System.out.println("\n테스트 종료! 총 문제 수: " + sum + ", 맞춘 문제 수: " + score + ", 틀린 문제 수: " + unscore);
        TestResult result = new TestResult(LocalDateTime.now(), score);
        saveResultToFile(result);
    }

    private static void showAnalysisReport() {
        List<TestResult> allResults = loadAllResultsFromFile(); // 파일로 부터 모든 결과를 꺼내옴
        if (allResults.isEmpty()) {
            System.out.println("분석할 데이터가 없습니다.");
            return;
        }

        Map<Integer, List<TestResult>> groupedResults = allResults.stream()
                .collect(Collectors.groupingBy(result -> getTimeSlotKey(result.getTestDateTime())));

        System.out.println("\n--- 최종 집중력 리포트 ---");

        double maxAverageScore = -1.0;
        int goldenTimeSlot = -1;

        for (int timeSlot : new int[]{9, 13, 17, 21}) { // 오전 9시, 오후 1시, 오후5시, 오후9시
            List<TestResult> resultsInSlot = groupedResults.get(timeSlot);
            if (resultsInSlot == null || resultsInSlot.isEmpty()) {
                System.out.printf("[%s] 데이터 없음\n", formatTimeSlot(timeSlot));
                continue;
            }

            double avgTaskScore = resultsInSlot.stream().mapToInt(TestResult::getTaskScore).average().orElse(0.0);
            System.out.printf("[%s] 평균 정답 개수: %.1f (테스트 %d회)\n",
                    formatTimeSlot(timeSlot), avgTaskScore, resultsInSlot.size());

            if (avgTaskScore > maxAverageScore) {
                maxAverageScore = avgTaskScore;
                goldenTimeSlot = timeSlot;
            }
        }

        if (goldenTimeSlot != -1) {
            System.out.printf("\n 당신의 골든타임은 [%s] 입니다! \n", formatTimeSlot(goldenTimeSlot));
        } else {
            System.out.println("\n아직 골든타임을 판단하기에 데이터가 부족합니다.");
        }
    }
    private static int getTimeSlotKey(LocalDateTime dateTime) {
        int hour = dateTime.getHour();
        if (hour >= 8 && hour < 12) return 9;
        if (hour >= 12 && hour < 16) return 13;
        if (hour >= 16 && hour < 20) return 17;
        return 21;
    }
    private static String formatTimeSlot(int timeSlot) {
        switch (timeSlot) {
            case 9: return "오전 9시";
            case 13: return "오후 1시";
            case 17: return "오후 5시";
            case 21: return "밤 9시";
            default: return "알 수 없음";
        }
    }
    private static void saveResultToFile(TestResult result) {
        try (FileWriter fw = new FileWriter(FILENAME, true);
             BufferedWriter bw = new BufferedWriter(fw);
             PrintWriter out = new PrintWriter(bw)) {
            out.println(result.toCsvString());
            System.out.println("결과가 성공적으로 저장되었습니다.");
        } catch (IOException e) {
            System.err.println("파일 저장 중 오류가 발생했습니다: " + e.getMessage());
        }
    }
    private static List<TestResult> loadAllResultsFromFile() {
        List<TestResult> results = new ArrayList<>();
        if (!Files.exists(Paths.get(FILENAME))) {
            return results;
        }
        try {
            results = Files.lines(Paths.get(FILENAME))
                    .map(TestResult::fromCsvString)
                    .collect(Collectors.toList());
        } catch (IOException e) {
            System.err.println("파일을 읽는 중 오류가 발생했습니다: " + e.getMessage());
        }
        return results;
    }
}