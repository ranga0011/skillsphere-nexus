package com.skillsphere.skill.entity;

public class CompetencyGap {
    private Long skillId;
    private String skillName;
    private Integer requiredProficiency;
    private Integer employeeProficiency;
    private Integer gap;
    private String status;
    private String evidenceStatus;
    private String recommendation;

    public CompetencyGap() {
    }

    public CompetencyGap(Long skillId, String skillName, Integer requiredProficiency, Integer employeeProficiency, Integer gap, String status) {
        this(skillId, skillName, requiredProficiency, employeeProficiency, gap, status, null, null);
    }

    public CompetencyGap(Long skillId, String skillName, Integer requiredProficiency,
            Integer employeeProficiency, Integer gap, String status,
            String evidenceStatus, String recommendation) {
        this.skillId = skillId;
        this.skillName = skillName;
        this.requiredProficiency = requiredProficiency;
        this.employeeProficiency = employeeProficiency;
        this.gap = gap;
        this.status = status;
        this.evidenceStatus = evidenceStatus;
        this.recommendation = recommendation;
    }

    public Long getSkillId() {
        return skillId;
    }

    public void setSkillId(Long skillId) {
        this.skillId = skillId;
    }

    public String getSkillName() {
        return skillName;
    }

    public void setSkillName(String skillName) {
        this.skillName = skillName;
    }

    public Integer getRequiredProficiency() {
        return requiredProficiency;
    }

    public void setRequiredProficiency(Integer requiredProficiency) {
        this.requiredProficiency = requiredProficiency;
    }

    public Integer getEmployeeProficiency() {
        return employeeProficiency;
    }

    public void setEmployeeProficiency(Integer employeeProficiency) {
        this.employeeProficiency = employeeProficiency;
    }

    public Integer getGap() {
        return gap;
    }

    public void setGap(Integer gap) {
        this.gap = gap;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getEvidenceStatus() {
        return evidenceStatus;
    }

    public void setEvidenceStatus(String evidenceStatus) {
        this.evidenceStatus = evidenceStatus;
    }

    public String getRecommendation() {
        return recommendation;
    }

    public void setRecommendation(String recommendation) {
        this.recommendation = recommendation;
    }
}
