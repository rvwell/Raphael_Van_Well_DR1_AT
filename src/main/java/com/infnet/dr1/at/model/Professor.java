package com.infnet.dr1.at.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;
import java.util.Set;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "professores")
public class Professor {
    @Id
    private String id;

    @Indexed(unique = true)
    private String username;

    private String password;

    private Set<String> roles;
}
