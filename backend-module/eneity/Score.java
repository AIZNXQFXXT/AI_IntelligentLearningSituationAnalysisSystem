@Data
public class Score {
    private int id;
    private int studentId;
    private int examId;
    private int courseId;
    private double regularScore;
    private double examScore;
    private double finalScore;
    private int rankClass;
    private int rankGrade;
    private int isAbsent;
    private int isCheat;
    private String auditStatus;
    private int enteredBy;
    private String reason;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private boolean isDeleted;
}
