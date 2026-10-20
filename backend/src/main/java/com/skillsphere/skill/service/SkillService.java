
package com.skillsphere.skill.service;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.skillsphere.skill.entity.Skill;
import com.skillsphere.skill.repository.SkillRepository;

@Service
@Transactional
public class SkillService {

    private final SkillRepository skillRepository;

    public SkillService(SkillRepository skillRepository) {
        this.skillRepository = skillRepository;
    }

    public List<Skill> getAllSkills() {
        return skillRepository.findAll();
    }

    public Optional<Skill> getSkillById(Long id) {
        return skillRepository.findById(id);
    }

    public Skill createSkill(Skill skill) {
        return skillRepository.save(skill);
    }

    public Skill updateSkill(Long id, Skill updatedSkill) {
        Skill existingSkill = skillRepository.findById(id)
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "Skill not found with id: " + id));

        existingSkill.setName(updatedSkill.getName());
        existingSkill.setCategory(updatedSkill.getCategory());
        existingSkill.setDescription(updatedSkill.getDescription());

        return skillRepository.save(existingSkill);
    }

    public void deleteSkill(Long id) {
        Skill existingSkill = skillRepository.findById(id)
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "Skill not found with id: " + id));

        skillRepository.delete(existingSkill);
    }
}
