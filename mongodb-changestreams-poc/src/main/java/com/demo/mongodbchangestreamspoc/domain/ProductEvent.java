package com.demo.mongodbchangestreamspoc.domain;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.ZonedDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProductEvent {

    private ZonedDateTime timeStamp;

    private String eventType;

    private Product fullDocument;

    private String documentId;

}
