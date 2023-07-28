package edu.ucsb.cs156.example.entities;

import java.time.LocalDateTime;

import javax.persistence.Entity;
import javax.persistence.Id;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Entity(name = "recommendationrequests")
public class RecommendationRequest {
    @Id
    private String requesterEmail;
    private String professorEmail;
    private String explanation;
    private LocalDateTime dateRequested;
    private LocalDateTime dateNeeded;
    private Boolean done; 
}