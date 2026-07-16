package com.campus.common.constant;

public class PromptTemplate {

    private PromptTemplate() {}

    public static final String DIAGNOSIS_PROMPT = """
你是一位经验丰富的校园学情分析专家。请根据以下学生成绩数据，生成一份详细的学情诊断报告。

学生信息：
- 姓名：%s
- 年级：%s
- 班级：%s

成绩数据（各科历次考试成绩）：
%s

请以JSON格式返回分析结果，包含以下字段：
1. overall: 整体学习情况综合评价（100-200字）
2. strengths: 优势学科列表（数组格式，每项包含subject和reason）
3. weaknesses: 薄弱学科列表（数组格式，每项包含subject和reason）
4. trend: 成绩趋势分析（50-100字）
5. suggestions: 学习建议列表（数组格式，每项建议50字以内）
6. riskLevel: 风险等级（HIGH/MEDIUM/LOW）

请确保JSON格式合法，不要包含markdown代码块标记。
""";

    public static final String COMMENT_PROMPT = """
你是一位班主任教师。请根据以下学生本学期成绩数据，生成一段期末评语。

学生信息：
- 姓名：%s
- 班级：%s

本学期成绩数据：
%s

要求：
1. 评语应个性化、有针对性，避免模板化
2. 先肯定优点，再指出不足，最后给出建议
3. 语气亲切、鼓励为主
4. 字数在80-150字之间
5. 直接返回评语文案，不要JSON包裹
""";

    public static final String RISK_ANALYSIS_PROMPT = """
你是一位学业预警分析专家。以下学生已被初步标记为潜在学业风险对象，请根据详细数据给出最终风险判定。

学生信息：
- 姓名：%s
- 班级：%s
- 当前风险指标：%s

详细成绩数据：
%s

请以JSON格式返回分析结果：
1. riskLevel: HIGH/MEDIUM/LOW
2. riskReason: 风险原因详细说明（50-100字）
3. riskFactors: 风险因素列表（数组）
4. recommendations: 干预建议列表（数组）

请确保JSON格式合法，不要包含markdown代码块标记。
""";

    public static final String SUGGESTION_PROMPT = """
你是一位资深学习规划师。请根据以下学生的学情诊断结果，生成个性化学习提升方案。

学生信息：
- 姓名：%s
- 年级：%s

学情诊断摘要：
%s

请以JSON格式返回提升方案：
1. shortTerm: 短期目标与行动（3-5条）
2. longTerm: 长期学习规划建议（2-3条）
3. dailyPlan: 每日学习时间分配建议
4. resources: 推荐学习资源（按学科）

请确保JSON格式合法，不要包含markdown代码块标记。
""";
}