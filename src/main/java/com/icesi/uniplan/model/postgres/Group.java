package com.icesi.uniplan.model.postgres;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.FetchType;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Table;
import java.util.ArrayList;
import java.util.List;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Entity
@Table(name = "groups")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Group {
    @Id
    @Column(length = 10)
    private String nrc;

    @Column(nullable = false)
    private Integer number;

    @Column(nullable = false, length = 6)
    private String semester;

    @ManyToOne
    @JoinColumn(name = "subject_code", nullable = false)
    private Subject subject;

    @ManyToOne
    @JoinColumn(name = "professor_id", nullable = false)
    private Employee professor;

    @OneToMany(mappedBy = "group", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    @JsonIgnore
    @ToString.Exclude
    private List<Enrollment> enrollments = new ArrayList<>();
}
