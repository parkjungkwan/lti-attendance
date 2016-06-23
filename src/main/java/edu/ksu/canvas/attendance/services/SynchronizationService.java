package edu.ksu.canvas.attendance.services;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import edu.ksu.canvas.attendance.entity.AttendanceCourse;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import edu.ksu.canvas.attendance.entity.AviationSection;
import edu.ksu.canvas.attendance.entity.AviationStudent;
import edu.ksu.canvas.attendance.repository.AviationCourseRepository;
import edu.ksu.canvas.attendance.repository.AviationSectionRepository;
import edu.ksu.canvas.attendance.repository.AviationStudentRepository;
import edu.ksu.canvas.error.NoLtiSessionException;
import edu.ksu.canvas.model.Enrollment;
import edu.ksu.canvas.model.Section;



@Component
@Scope(value="session")
public class SynchronizationService {

    public static final int DEFAULT_TOTAL_CLASS_MINUTES = 2160; //DEFAULT_MINUTES_PER_CLASS * 3 days a week * 16 weeks
    public static final int DEFAULT_MINUTES_PER_CLASS = 45;

    private static final Logger LOG = Logger.getLogger(SynchronizationService.class);

    @Autowired
    private AviationCourseRepository aviationCourseRepository;

    @Autowired
    private AviationStudentRepository studentRepository;

    @Autowired
    private AviationSectionRepository sectionRepository;

    @Autowired
    private CanvasApiWrapperService canvasService;


    public void synchronizeWhenCourseNotExistsInDB(long canvasCourseId) throws NoLtiSessionException {
        if (aviationCourseRepository.findByCanvasCourseId(canvasCourseId) == null) {
            synchronize(canvasCourseId);
        }
    }

    public void synchronize(long canvasCourseId) throws NoLtiSessionException {
        List<Section> sections = canvasService.getSections(canvasCourseId);

        synchronizeCourseFromCanvasToDb(canvasCourseId);
        synchronizeSectionsFromCanvasToDb(sections);

        Map<Section, List<Enrollment>> canvasSectionMap = canvasService.getEnrollmentsFromCanvas(sections);
        synchronizeStudentsFromCanvasToDb(canvasSectionMap);
    }

    private AttendanceCourse synchronizeCourseFromCanvasToDb(long canvasCourseId) {
        AttendanceCourse attendanceCourse = aviationCourseRepository.findByCanvasCourseId(canvasCourseId);

        if (attendanceCourse == null) {
            attendanceCourse = new AttendanceCourse();
            attendanceCourse.setTotalMinutes(DEFAULT_TOTAL_CLASS_MINUTES);
            attendanceCourse.setDefaultMinutesPerSession(DEFAULT_MINUTES_PER_CLASS);
            attendanceCourse.setCanvasCourseId(canvasCourseId);
        }

        return aviationCourseRepository.save(attendanceCourse);
    }

    private List<AviationSection> synchronizeSectionsFromCanvasToDb(List<Section> sections) {
        List<AviationSection> ret = new ArrayList<>();

        for (Section section : sections) {
            AviationSection aviationSection = sectionRepository.findByCanvasSectionId(Long.valueOf(section.getId()));

            if (aviationSection == null) {
                aviationSection = new AviationSection();
            }

            aviationSection.setName(section.getName());
            aviationSection.setCanvasSectionId(Long.valueOf(section.getId()));
            aviationSection.setCanvasCourseId(Long.valueOf(section.getCourseId()));

            ret.add(sectionRepository.save(aviationSection));
        }

        return ret;
    }

    private List<AviationStudent> synchronizeStudentsFromCanvasToDb(Map<Section, List<Enrollment>> canvasSectionMap) {
        List<AviationStudent> ret = new ArrayList<>();
        List<AviationStudent> existingStudentsInDb = null;
        Set<AviationStudent> droppedStudents = new HashSet<>();

        for(Section section: canvasSectionMap.keySet()) {

            if(existingStudentsInDb == null) {
                existingStudentsInDb = studentRepository.findByCanvasCourseId(section.getCourseId());
                droppedStudents.addAll(existingStudentsInDb);
            }

            for(Enrollment enrollment: canvasSectionMap.get(section)) {

                Optional<AviationStudent> foundUser = 
                        existingStudentsInDb.stream()
                                        .filter(u -> u.getSisUserId().equals(enrollment.getUser().getSisUserId()))
                                        .findFirst();

                if (foundUser.isPresent()){
                    droppedStudents.remove(foundUser.get());
                }
                AviationStudent student = foundUser.isPresent() ? foundUser.get() : new AviationStudent();
                student.setSisUserId(enrollment.getUser().getSisUserId());
                student.setName(enrollment.getUser().getSortableName());
                student.setCanvasSectionId(section.getId());
                student.setCanvasCourseId(section.getCourseId() == null ? null : Long.valueOf(section.getCourseId()));
                student.setDeleted(foundUser.isPresent() ? foundUser.get().getDeleted() : Boolean.FALSE);

                ret.add(studentRepository.save(student));
            }

        }
        addDroppedStudents(ret, droppedStudents);

        return ret;
    }

    private void addDroppedStudents(List<AviationStudent> studentList, Set<AviationStudent> droppedStudents) {
        if (!droppedStudents.isEmpty()){
            droppedStudents.forEach(student -> {
                student.setDeleted(true);
                studentList.add(studentRepository.save(student));
                LOG.debug("Added dropped student to course list: " + student.getName());
            });
        }
    }

}
