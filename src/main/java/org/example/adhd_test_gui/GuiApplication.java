package org.example.adhd_test_gui;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Application;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

public class GuiApplication extends Application {

    // --- 클래스 멤버 변수 선언 ---
    private Stage primaryStage;
    private Scene startScene, testScene, resultScene;

    // UI 요소들
    private Label problemLabel = new Label();
    private TextField answerField = new TextField();
    private Label scoreLabel = new Label("맞춘 개수: 0");
    private Label timerLabel = new Label("남은 시간: 30초");
    private Label totalProblemsLabel = new Label();
    private Label correctAnswersLabel = new Label();
    private Label incorrectAnswersLabel = new Label();

    // 테스트 로직 관련 변수들
    private int score = 0;
    private int correctAnswer;
    private Random random = new Random();
    private Timeline timer;
    private int timeLeft = 30;
    private boolean isTestRunning = false;
    private int problemsAttempted = 0;

    private static final String FILENAME = "math_results.csv";

    @Override
    public void start(Stage primaryStage) {
        this.primaryStage = primaryStage;
        primaryStage.setTitle("Golden Time Finder");

        // 1. 시작 화면 구성
        Label welcomeLabel = new Label("집중력 테스트 프로그램");
        Button startButton = new Button("테스트 시작");
        Button reportButton = new Button("결과 리포트 보기");
        startButton.setOnAction(event -> startTest());
        reportButton.setOnAction(event -> showAnalysisReport());
        VBox startLayout = new VBox(20, welcomeLabel, startButton, reportButton);
        startLayout.setAlignment(Pos.CENTER);
        startScene = new Scene(startLayout, 400, 300);

        // 2. 테스트 화면 구성
        answerField.setPromptText("정답을 입력하고 Enter");
        answerField.setOnAction(event -> checkAnswer());
        VBox testLayout = new VBox(20, timerLabel, problemLabel, answerField, scoreLabel);
        testLayout.setAlignment(Pos.CENTER);
        testScene = new Scene(testLayout, 400, 300);

        // 3. 결과 화면 구성
        Label resultTitle = new Label("테스트 결과");
        Button okButton = new Button("확인");
        okButton.setOnAction(event -> primaryStage.setScene(startScene));
        VBox resultLayout = new VBox(20, resultTitle, totalProblemsLabel, correctAnswersLabel, incorrectAnswersLabel, okButton);
        resultLayout.setAlignment(Pos.CENTER);
        resultScene = new Scene(resultLayout, 400, 300);


        primaryStage.setScene(startScene);
        primaryStage.show();
    }

    private void startTest() {
        isTestRunning = true;
        answerField.setEditable(true);
        score = 0;
        problemsAttempted = 0;
        timeLeft = 30;
        scoreLabel.setText("맞춘 개수: 0");
        timerLabel.setText("남은 시간: 30초");

        generateNewProblem();
        setupTimer();
        timer.play();

        primaryStage.setScene(testScene);
    }

    private void endTest() {
        isTestRunning = false;
        answerField.setEditable(false);

        // 결과 계산
        int incorrectAnswers = problemsAttempted - score;

        // 결과 화면의 Label 텍스트 업데이트
        totalProblemsLabel.setText("푼 문제 수: " + problemsAttempted);
        correctAnswersLabel.setText("맞힌 문제 수: " + score);
        incorrectAnswersLabel.setText("틀린 문제 수: " + incorrectAnswers);

        // 결과 객체 생성 및 파일에 저장
        TestResult result = new TestResult(LocalDateTime.now(), score);
        saveResultToFile(result);

        // 결과 화면으로 전환
        primaryStage.setScene(resultScene);
    }

    private void generateNewProblem() {
        int num1, num2;
        String problemText;

        int operator = random.nextInt(4); // 0:+, 1:-, 2:*, 3:/
        switch (operator) {
            case 0: // 덧셈
                num1 = random.nextInt(50) + 1;
                num2 = random.nextInt(50) + 1;
                correctAnswer = num1 + num2;
                problemText = String.format("%d + %d = ?", num1, num2);
                break;
            case 1: // 뺄셈
                num1 = random.nextInt(50) + 1;
                num2 = random.nextInt(50) + 1;
                correctAnswer = Math.max(num1, num2) - Math.min(num1, num2);
                problemText = String.format("%d - %d = ?", Math.max(num1, num2), Math.min(num1, num2));
                break;
            case 2: // 곱셈
                num1 = random.nextInt(9) + 2;
                num2 = random.nextInt(9) + 2;
                correctAnswer = num1 * num2;
                problemText = String.format("%d * %d = ?", num1, num2);
                break;
            case 3: // 나눗셈
                int divisor = random.nextInt(9) + 2;
                correctAnswer = random.nextInt(9) + 2;
                int dividend = divisor * correctAnswer;
                problemText = String.format("%d / %d = ?", dividend, divisor);
                break;
            default:
                problemText = "Error";
                correctAnswer = 0;
        }
        problemLabel.setText(problemText);
        answerField.clear();
    }

    private void checkAnswer() {
        if (!isTestRunning) {
            return;
        }

        try {
            problemsAttempted++;
            int userAnswer = Integer.parseInt(answerField.getText());
            if (userAnswer == correctAnswer) {
                score++;
                scoreLabel.setText("맞춘 개수: " + score);
            }
            generateNewProblem();
        } catch (NumberFormatException e) {
            // 숫자가 아니면 무시
        }
    }

    private void setupTimer() {
        timer = new Timeline(new KeyFrame(Duration.seconds(1), event -> {
            timeLeft--;
            timerLabel.setText("남은 시간: " + timeLeft + "초");
            if (timeLeft <= 0) {
                timer.stop();
                endTest();
            }
        }));
        timer.setCycleCount(Timeline.INDEFINITE);
    }

    private void showAnalysisReport() {
        List<TestResult> allResults = loadAllResultsFromFile();
        if (allResults.isEmpty()) {
            Alert alert = new Alert(Alert.AlertType.INFORMATION, "분석할 데이터가 없습니다.");
            alert.showAndWait();
            return;
        }

        Map<Integer, List<TestResult>> groupedResults = allResults.stream()
                .collect(Collectors.groupingBy(result -> getTimeSlotKey(result.getTestDateTime())));

        StringBuilder reportContent = new StringBuilder();
        double maxAverageScore = -1.0;
        int goldenTimeSlot = -1;

        for (int timeSlot : new int[]{9, 13, 17, 21}) {
            List<TestResult> resultsInSlot = groupedResults.get(timeSlot);
            if (resultsInSlot == null || resultsInSlot.isEmpty()) {
                reportContent.append(String.format("[%s] 데이터 없음\n", formatTimeSlot(timeSlot)));
                continue;
            }
            double avgTaskScore = resultsInSlot.stream().mapToInt(TestResult::getTaskScore).average().orElse(0.0);
            reportContent.append(String.format("[%s] 평균 정답 개수: %.1f (테스트 %d회)\n",
                    formatTimeSlot(timeSlot), avgTaskScore, resultsInSlot.size()));
            if (avgTaskScore > maxAverageScore) {
                maxAverageScore = avgTaskScore;
                goldenTimeSlot = timeSlot;
            }
        }

        if (goldenTimeSlot != -1) {
            reportContent.append(String.format("\n✨ 당신의 골든타임은 [%s] 입니다! ✨", formatTimeSlot(goldenTimeSlot)));
        }

        Alert reportAlert = new Alert(Alert.AlertType.INFORMATION);
        reportAlert.setTitle("최종 집중력 리포트");
        reportAlert.setHeaderText("시간대별 분석 결과입니다.");
        reportAlert.setContentText(reportContent.toString());
        reportAlert.showAndWait();
    }

    private int getTimeSlotKey(LocalDateTime dateTime) {
        int hour = dateTime.getHour();
        if (hour >= 8 && hour < 12) return 9;
        if (hour >= 12 && hour < 16) return 13;
        if (hour >= 16 && hour < 20) return 17;
        return 21;
    }

    private String formatTimeSlot(int timeSlot) {
        switch (timeSlot) {
            case 9: return "오전 9시";
            case 13: return "오후 1시";
            case 17: return "오후 5시";
            case 21: return "밤 9시";
            default: return "알 수 없음";
        }
    }

    private void saveResultToFile(TestResult result) {
        try (FileWriter fw = new FileWriter(FILENAME, true);
             BufferedWriter bw = new BufferedWriter(fw);
             PrintWriter out = new PrintWriter(bw)) {
            out.println(result.toCsvString());
            System.out.println("결과가 성공적으로 저장되었습니다.");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private List<TestResult> loadAllResultsFromFile() {
        List<TestResult> results = new ArrayList<>();
        if (!Files.exists(Paths.get(FILENAME))) {
            return results;
        }
        try {
            results = Files.lines(Paths.get(FILENAME))
                    .map(TestResult::fromCsvString)
                    .collect(Collectors.toList());
        } catch (IOException e) {
            e.printStackTrace();
        }
        return results;
    }

    public static void main(String[] args) {
        launch(args);
    }
}