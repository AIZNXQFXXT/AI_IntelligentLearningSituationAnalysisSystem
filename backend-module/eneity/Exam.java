@Data
public class Exam {
    private int id;
    private String name;
    private String type;
    private String semester;
    private int classId;
    private LocalDate examDate;
    private int isArchived;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private boolean isDeleted;
}
