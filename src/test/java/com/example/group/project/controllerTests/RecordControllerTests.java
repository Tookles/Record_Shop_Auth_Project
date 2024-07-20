package com.example.group.project.controllerTests;

import com.example.group.project.controller.RecordController;
import com.example.group.project.model.Record;
import com.example.group.project.model.RecordRepository;
import io.restassured.module.mockmvc.RestAssuredMockMvc;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import static org.hamcrest.Matchers.equalTo;
import java.util.List;


@ExtendWith(MockitoExtension.class)
public class RecordControllerTests {
    @Mock
    private RecordRepository recordRepository;

    @InjectMocks
    private RecordController recordController;

    @BeforeEach
    public void setUpMockRecordController() {
        RestAssuredMockMvc.standaloneSetup(recordController);
    }

    // todo autowire the baseURI for docker
    @BeforeAll
    static void setup(){
        String baseURI = "http://localhost:8080";
    }

    @Test
    public void getRecord_givenExistingArtist_returnsArtistRecords () {
        Record record1 = new Record();
        Record record2 = new Record();

        record1.setName("Rec1");
        record2.setName("Rec2");

        String artist = "Michael Jackson";

        record1.setArtist(artist);
        record2.setArtist(artist);

        List<Record> recordsByArtist = List.of(record1, record2);

        Mockito
                .when(recordRepository.findByArtistIgnoreCase(artist))
                .thenReturn(recordsByArtist);


        RestAssuredMockMvc
                .given()
                    .param("artist", "Michael Jackson")
                .when()
                    .get("/getRecord")
                .then()
                    .statusCode(200)
                    .body("$.size()", equalTo(recordsByArtist.size()))
                    .body("[0].artist", equalTo(record1.getArtist()))
                    .body("[1].artist", equalTo(record2.getArtist()));
    }

    @Test
    public void getRecord_givenExistingRecord_returnsThatRecord () {
        String recordName = "Rec1";
        Record record1 = new Record();
        record1.setName(recordName);

        List<Record> recordByName = List.of(record1);

        Mockito
                .when(recordRepository.findByNameIgnoreCase(recordName))
                .thenReturn(recordByName);


        RestAssuredMockMvc
                .given()
                .param("name", "Rec1")
                .when()
                .get("/getRecord")
                .then()
                .statusCode(200)
                .body("$.size()", equalTo(recordByName.size()))
                .body("[0].name", equalTo(record1.getName()));
    }

    @Test
    public void getRecord_givenNoParam_returnsAllRecords () {
        Record record1 = new Record();
        Record record2 = new Record();
        Record record3 = new Record();

        record1.setName("Rec1");
        record2.setName("Rec2");
        record3.setName("Rec3");

        List<Record> allRecords = List.of(record1, record2, record3);

        Mockito
                .when(recordRepository.findAll())
                .thenReturn(allRecords);


        RestAssuredMockMvc
                .when()
                .get("/getRecord")
                .then()
                .statusCode(200)
                .body("$.size()", equalTo(allRecords.size()))
                .body("[0].name", equalTo(record1.getName()))
                .body("[1].name", equalTo(record2.getName()))
                .body("[2].name", equalTo(record3.getName()));
    }

}
