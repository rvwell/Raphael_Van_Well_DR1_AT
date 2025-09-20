package com.infnet.dr1.at.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "disciplinas")
public class Disciplina {
    @Id
    private String id;

    private String nome;

    @Indexed(unique = true)
    private String codigo;

    @Indexed
    private String professorId;
}
