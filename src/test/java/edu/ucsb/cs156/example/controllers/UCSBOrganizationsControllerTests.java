package edu.ucsb.cs156.example.controllers;


import edu.ucsb.cs156.example.repositories.UserRepository;
import edu.ucsb.cs156.example.testconfig.TestConfig;
import edu.ucsb.cs156.example.ControllerTestCase;
import edu.ucsb.cs156.example.entities.UCSBDiningCommons;
import edu.ucsb.cs156.example.entities.UCSBOrganizations;
import edu.ucsb.cs156.example.repositories.UCSBOrganizationsRepository;


import java.util.ArrayList;
import java.util.Arrays;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MvcResult;


import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;


import java.util.Optional;


import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;


@WebMvcTest(controllers = UCSBOrganizationsController.class)
@Import(TestConfig.class)
public class UCSBOrganizationsControllerTests extends ControllerTestCase {


    @MockBean
    UCSBOrganizationsRepository ucsbOrganizationsRepository;


    @MockBean
    UserRepository userRepository;


    // Authorization tests for /api/ucsborganizations/admin/all


    @Test
    public void logged_out_users_cannot_get_all() throws Exception {
            mockMvc.perform(get("/api/ucsborganizations/all"))
                            .andExpect(status().is(403)); // logged out users can't get all
    }


    @WithMockUser(roles = { "USER" })
    @Test
    public void logged_in_users_can_get_all() throws Exception {
            mockMvc.perform(get("/api/ucsborganizations/all"))
                            .andExpect(status().is(200)); // logged
    }


    @Test
    public void logged_out_users_cannot_get_by_id() throws Exception {
            mockMvc.perform(get("/api/ucsborganizations?orgCode=tasa"))
                            .andExpect(status().is(403)); // logged out users can't get by orgCode
    }


    // Authorization tests for /api/ucsborganizations/post


    @Test
    public void logged_out_users_cannot_post() throws Exception {
            mockMvc.perform(post("/api/ucsborganizations/post"))
                            .andExpect(status().is(403));
    }


    @WithMockUser(roles = { "USER" })
    @Test
    public void logged_in_regular_users_cannot_post() throws Exception {
            mockMvc.perform(post("/api/ucsborganizations/post"))
                            .andExpect(status().is(403)); // only admins can post
    }


    // Tests for database actions


    @WithMockUser(roles = { "USER" })
        @Test
        public void test_that_logged_in_user_can_get_by_id_when_the_id_exists() throws Exception {


                // arrange


                UCSBOrganizations organizations = UCSBOrganizations.builder()
                                .orgCode("tasa")
                                .orgTranslationShort("taiwaneseAmericanStudentAssociation")
                                .orgTranslation("taiwaneseAmericanStudentAssociationAtUCSB")
                                .inactive(false)
                                .build();


                when(ucsbOrganizationsRepository.findById(eq("tasa"))).thenReturn(Optional.of(organizations));


                // act
                MvcResult response = mockMvc.perform(get("/api/ucsborganizations?orgCode=tasa"))
                                .andExpect(status().isOk()).andReturn();


                // assert


                verify(ucsbOrganizationsRepository, times(1)).findById(eq("tasa"));
                String expectedJson = mapper.writeValueAsString(organizations);
                String responseString = response.getResponse().getContentAsString();
                assertEquals(expectedJson, responseString);
   }


    @WithMockUser(roles = { "USER" })
    @Test
    public void test_that_logged_in_user_can_get_by_id_when_the_id_does_not_exist() throws Exception {


                when(ucsbOrganizationsRepository.findById(eq("club-Basketball"))).thenReturn(Optional.empty());


                MvcResult response = mockMvc.perform(get("/api/ucsborganizations?orgCode=club-Basketball"))
                                .andExpect(status().isNotFound()).andReturn();
               
                verify(ucsbOrganizationsRepository, times(1)).findById(eq("club-Basketball"));
                Map<String, Object> json = responseToJson(response);
                assertEquals("EntityNotFoundException", json.get("type"));
                assertEquals("UCSBOrganizations with id club-Basketball not found", json.get("message"));
    }


    @WithMockUser(roles = { "USER" })
    @Test
    public void logged_in_user_can_get_all_ucsborganizations() throws Exception {


                UCSBOrganizations tasa = UCSBOrganizations.builder()
                                .orgCode("tasa")
                                .orgTranslationShort("taiwaneseAmericanStudentAssociation")
                                .orgTranslation("taiwaneseAmericanStudentAssociationAtUCSB")
                                .inactive(false)
                                .build();


                UCSBOrganizations osli = UCSBOrganizations.builder()
                                .orgCode("osli")
                                .orgTranslationShort("studentLife")
                                .orgTranslation("officeOfStudentLife")
                                .inactive(false)
                                .build();


                ArrayList<UCSBOrganizations> expectedOrganizations = new ArrayList<>();
                expectedOrganizations.addAll(Arrays.asList(tasa, osli));


                when(ucsbOrganizationsRepository.findAll()).thenReturn(expectedOrganizations);


                MvcResult response = mockMvc.perform(get("/api/ucsborganizations/all"))
                                .andExpect(status().isOk()).andReturn();
               
                verify(ucsbOrganizationsRepository, times(1)).findAll();
                String expectedJson = mapper.writeValueAsString(expectedOrganizations);
                String responseString = response.getResponse().getContentAsString();
                assertEquals(expectedJson, responseString);
    }


    @WithMockUser(roles = { "ADMIN", "USER" })
    @Test
    public void an_admin_user_can_post_a_new_organizations() throws Exception {


        UCSBOrganizations krc = UCSBOrganizations.builder()
                        .orgCode("krc")
                        .orgTranslationShort("koreanRadioCl")
                        .orgTranslation("koreanRadioClub")
                        .inactive(true)
                        .build();


        when(ucsbOrganizationsRepository.save(eq(krc))).thenReturn(krc);


        MvcResult response = mockMvc.perform(post("/api/ucsborganizations/post?orgCode=krc&orgTranslationShort=koreanRadioCl&orgTranslation=koreanRadioClub&inactive=true").with(csrf()))
                        .andExpect(status().isOk()).andReturn();
       
        verify(ucsbOrganizationsRepository, times(1)).save(krc);
        String expectedJson = mapper.writeValueAsString(krc);
        String responseString = response.getResponse().getContentAsString();
        assertEquals(expectedJson, responseString);
    }

    @WithMockUser(roles = { "ADMIN", "USER" })
    @Test
    public void admin_can_delete_a_organization() throws Exception {

        UCSBOrganizations sky = UCSBOrganizations.builder()
                        .orgCode("sky")
                        .orgTranslationShort("skydivingClub")
                        .orgTranslation("skydivingClubAtUCSB")
                        .inactive(false)
                        .build();
        
        when(ucsbOrganizationsRepository.findById(eq("sky"))).thenReturn(Optional.of(sky));

        MvcResult response = mockMvc.perform(
                        delete("/api/ucsborganizations?orgCode=sky")
                                        .with(csrf()))
                        .andExpect(status().isOk()).andReturn();
        
        verify(ucsbOrganizationsRepository, times(1)).findById("sky");
        verify(ucsbOrganizationsRepository, times(1)).delete(any());

        Map<String, Object> json = responseToJson(response);
        assertEquals("UCSBOrganization with id sky deleted", json.get("message"));
    }

    @WithMockUser(roles = { "ADMIN", "USER"})
    @Test
    public void admin_tries_to_delete_non_existant_organization_and_gets_right_error_message() throws Exception {

        when(ucsbOrganizationsRepository.findById(eq("sky"))).thenReturn(Optional.empty());

        MvcResult response = mockMvc.perform(
                        delete("/api/ucsborganizations?orgCode=sky")
                                .with(csrf()))
                        .andExpect(status().isNotFound()).andReturn();
        
        verify(ucsbOrganizationsRepository, times(1)).findById("sky");
        Map<String, Object> json = responseToJson(response);
        assertEquals("UCSBOrganizations with id sky not found", json.get("message"));
    }

    @WithMockUser(roles = { "ADMIN", "USER" })
    @Test
    public void admin_can_edit_an_existing_organization() throws Exception {

        UCSBOrganizations clubBasketballOriginal = UCSBOrganizations.builder()
                        .orgCode("mcb")
                        .orgTranslationShort("mensClubBasketball")
                        .orgTranslation("mensClubBasketballAtUCSB")
                        .inactive(false)
                        .build();

        UCSBOrganizations clubBasketballEdited = UCSBOrganizations.builder()
                        .orgCode("ucsbcb")
                        .orgTranslationShort("mensClubBball")
                        .orgTranslation("ucsbMensClubBasketball")
                        .inactive(true)
                        .build();

        String requestBody = mapper.writeValueAsString(clubBasketballEdited);

        when(ucsbOrganizationsRepository.findById(eq("mcb"))).thenReturn(Optional.of(clubBasketballOriginal));

        MvcResult response = mockMvc.perform(
                        put("/api/ucsborganizations?orgCode=mcb")
                                        .contentType(MediaType.APPLICATION_JSON)
                                        .characterEncoding("utf-8")
                                        .content(requestBody)
                                        .with(csrf()))
                        .andExpect(status().isOk()).andReturn();
        
        verify(ucsbOrganizationsRepository, times(1)).findById("mcb");
        verify(ucsbOrganizationsRepository, times(1)).save(clubBasketballEdited);
        String responseString = response.getResponse().getContentAsString();
        assertEquals(requestBody, responseString);
    }

    @WithMockUser(roles = { "ADMIN", "USER" })
    @Test
    public void admin_cannot_edit_organizations_that_do_not_exist() throws Exception {

        UCSBOrganizations editedOrganization = UCSBOrganizations.builder()
                        .orgCode("sky")
                        .orgTranslationShort("skyDivingClub")
                        .orgTranslation("skyDivingClubAtUCSB")
                        .inactive(false)
                        .build();
        
        String requestBody = mapper.writeValueAsString(editedOrganization);

        when(ucsbOrganizationsRepository.findById(eq("sky"))).thenReturn(Optional.empty());

        MvcResult response = mockMvc.perform(
                        put("/api/ucsborganizations?orgCode=sky")
                                        .contentType(MediaType.APPLICATION_JSON)
                                        .characterEncoding("utf-8")
                                        .content(requestBody)
                                        .with(csrf()))
                        .andExpect(status().isNotFound()).andReturn();
        
        verify(ucsbOrganizationsRepository, times(1)).findById("sky");
        Map<String, Object> json = responseToJson(response);
        assertEquals("UCSBOrganizations with id sky not found", json.get("message"));
    }
}
