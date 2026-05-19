package com.icesi.uniplan.model.postgres;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "contract_types")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ContractType {
    @Id
    @Column(length = 30)
    private String name;
}
