package net.arvandor.numinoustreasury.profession;

public interface ExperienceUpdateCallback {
    void invoke(int oldExperience, int newExperience);
}
