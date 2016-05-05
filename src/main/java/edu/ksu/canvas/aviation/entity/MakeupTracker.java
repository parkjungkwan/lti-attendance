package edu.ksu.canvas.aviation.entity;

import javax.persistence.*;

import org.hibernate.annotations.Check;

import java.util.Date;


@Entity
@Table(name = "aviation_makeup_tracker")
@Check(constraints="minutes_madeup >= 0")
public class MakeupTracker {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "makeup_tracker_id")
    private int makeupTrackerId;

    @Temporal(TemporalType.DATE)
    @Column(name = "date_of_class")
    private Date dateOfClass;

    @Temporal(TemporalType.DATE)
    @Column(name = "date_madeup")
    private Date dateMadeUp;

    @Column(name = "minutes_madeup")
    private int minutesMadeUp;

    @ManyToOne
    @JoinColumn(name="student_id", foreignKey = @ForeignKey(name = "fk_student_for_makeup_tracker"), nullable=false)
    private Student student;

    public int getMakeupTrackerId() {
        return makeupTrackerId;
    }

    public void setMakeupTrackerId(int makeupTrackerId) {
        this.makeupTrackerId = makeupTrackerId;
    }

    public Date getDateOfClass() {
        return dateOfClass;
    }

    public void setDateOfClass(Date dateOfClass) {
        this.dateOfClass = dateOfClass;
    }

    public Date getDateMadeUp() {
        return dateMadeUp;
    }

    public void setDateMadeUp(Date dateMadeUp) {
        this.dateMadeUp = dateMadeUp;
    }

    public int getMinutesMadeUp() {
        return minutesMadeUp;
    }

    public void setMinutesMadeUp(int minutesMadeUp) {
        this.minutesMadeUp = minutesMadeUp;
    }

    public Student getStudent() {
        return student;
    }

    public void setStudent(Student student) {
        this.student = student;
    }
}