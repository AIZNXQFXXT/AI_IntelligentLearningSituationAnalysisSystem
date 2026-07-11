@Data
public class AIComment {
    private int id;
    private int studentId;
    private int teacherId;
    private String semester;
    private String content;
    private int isTeacherEdited;
    private String status;
    private String generatedBy;
    private int tokensUsed;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private boolean isDeleted;
}
