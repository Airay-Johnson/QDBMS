package SurveySystem.model;

public class Option {
    private String id;
    private String text;

    // 构造函数
    public Option() {}

    public Option(String id, String text) {
        this.id = id;
        this.text = text;
    }

    // Getter 和 Setter 方法
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    @Override
    public String toString() {
        return text; // 返回文本用于显示
    }
}