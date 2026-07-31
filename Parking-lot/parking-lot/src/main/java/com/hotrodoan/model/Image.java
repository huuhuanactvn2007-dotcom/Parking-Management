package com.hotrodoan.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.GenericGenerator;

@Entity
@Data
@NoArgsConstructor
public class Image {
    @Id
    @GeneratedValue(generator = "uuid")
    @GenericGenerator(name = "uuid", strategy = "uuid2")
    private String id;
    private String fileName;

    @Column(columnDefinition = "LONGBLOB")
    @Lob
    private byte[] data;

    private String fileType;

    public Image(String fileName, byte[] data, String fileType) {
        this.fileName = fileName;
        this.data = data;
        this.fileType = fileType;
    }
}
