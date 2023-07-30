package edu.ucsb.cs156.example.controllers;

import edu.ucsb.cs156.example.repositories.UserRepository;
import edu.ucsb.cs156.example.testconfig.TestConfig;
import edu.ucsb.cs156.example.ControllerTestCase;
import edu.ucsb.cs156.example.entities.UCSBDiningCommonsMenuItem;
import edu.ucsb.cs156.example.repositories.UCSBDiningCommonsMenuItemsRepository;

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

@WebMvcTest(controllers = UCSBDiningCommonsMenuItemController.class)
@Import(TestConfig.class)
public class UCSBDiningCommonMenuItemControllerTests extends ControllerTestCase{

        @MockBean
        UCSBDiningCommonsMenuItemsRepository ucsbDiningCommonsMenuItemsRepository;

        @MockBean
        UserRepository userRepository;

        @Test
        public void logged_out_users_cannot_get_all() throws Exception {
                mockMvc.perform(get("/api/ucsbdiningcommonsmenuitem/all"))
                                .andExpect(status().is(403)); // logged out users can't get all
        }

        @WithMockUser(roles = { "USER" })
        @Test
        public void logged_in_users_can_get_all() throws Exception {
                mockMvc.perform(get("/api/ucsbdiningcommonsmenuitem/all"))
                                .andExpect(status().is(200)); // logged
        }

        @Test
        public void logged_out_users_cannot_get_by_id() throws Exception {
                mockMvc.perform(get("/api/ucsbdiningcommonsmenuitem?id=1"))
                                .andExpect(status().is(403)); // logged out users can't get by id
        }

        @Test
        public void logged_out_users_cannot_post() throws Exception {
                mockMvc.perform(post("/api/ucsbdiningcommonsmenuitem/post"))
                                .andExpect(status().is(403));
        }

        @WithMockUser(roles = { "USER" })
        @Test
        public void logged_in_regular_users_cannot_post() throws Exception {
                mockMvc.perform(post("/api/ucsbdiningcommonsmenuitem/post"))
                                .andExpect(status().is(403)); // only admins can post
        }

        @WithMockUser(roles = { "USER" })
        @Test
        public void test_that_logged_in_user_can_get_by_id_when_the_id_exists() throws Exception {

                // arrange

                UCSBDiningCommonsMenuItem item = UCSBDiningCommonsMenuItem.builder()
                                .name("Baked Pesto Pasta with Chicken")
                                .diningCommonsCode("ortega")
                                .station("Entree Specials")
                                .build();

                when(ucsbDiningCommonsMenuItemsRepository.findById(eq(1L))).thenReturn(Optional.of(item));

                // act
                MvcResult response = mockMvc.perform(get("/api/ucsbdiningcommonsmenuitem?id=1"))
                                .andExpect(status().isOk()).andReturn();

                // assert

                verify(ucsbDiningCommonsMenuItemsRepository, times(1)).findById(eq(1L));
                String expectedJson = mapper.writeValueAsString(item);
                String responseString = response.getResponse().getContentAsString();
                assertEquals(expectedJson, responseString);
        }

        @WithMockUser(roles = { "USER" })
        @Test
        public void test_that_logged_in_user_can_get_by_id_when_the_id_does_not_exist() throws Exception {

                // arrange

                when(ucsbDiningCommonsMenuItemsRepository.findById(eq(114514L))).thenReturn(Optional.empty());

                // act
                MvcResult response = mockMvc.perform(get("/api/ucsbdiningcommonsmenuitem?id=114514"))
                                .andExpect(status().isNotFound()).andReturn();

                // assert

                verify(ucsbDiningCommonsMenuItemsRepository, times(1)).findById(eq(114514L));
                Map<String, Object> json = responseToJson(response);
                assertEquals("EntityNotFoundException", json.get("type"));
                assertEquals("UCSBDiningCommonsMenuItem with id 114514 not found", json.get("message"));
        }        

        @WithMockUser(roles = { "USER" })
        @Test
        public void logged_in_user_can_get_all_ucsbdiningcommons() throws Exception {

                // arrange

                UCSBDiningCommonsMenuItem BPPwC = UCSBDiningCommonsMenuItem.builder()
                                .name("Baked Pesto Pasta with Chicken")
                                .diningCommonsCode("ortega")
                                .station("Entree Specials")
                                .build();

                UCSBDiningCommonsMenuItem TBMS = UCSBDiningCommonsMenuItem.builder()
                                .name("Tofu Banh Mi Sandwich (v)")
                                .diningCommonsCode("ortega")
                                .station("Entree Specials")
                                .build();

                ArrayList<UCSBDiningCommonsMenuItem> expectedItems = new ArrayList<>();
                expectedItems.addAll(Arrays.asList(BPPwC, TBMS));

                when(ucsbDiningCommonsMenuItemsRepository.findAll()).thenReturn(expectedItems);

                // act
                MvcResult response = mockMvc.perform(get("/api/ucsbdiningcommonsmenuitem/all"))
                                .andExpect(status().isOk()).andReturn();

                // assert

                verify(ucsbDiningCommonsMenuItemsRepository, times(1)).findAll();
                String expectedJson = mapper.writeValueAsString(expectedItems);
                String responseString = response.getResponse().getContentAsString();
                assertEquals(expectedJson, responseString);
        }

        @WithMockUser(roles = { "ADMIN", "USER" })
        @Test
        public void an_admin_user_can_post_a_new_item() throws Exception {
                // arrange

                UCSBDiningCommonsMenuItem BPPwC = UCSBDiningCommonsMenuItem.builder()
                                .name("BakedPestoPastawithChicken")
                                .diningCommonsCode("ortega")
                                .station("EntreeSpecials")
                                .build();

                when(ucsbDiningCommonsMenuItemsRepository.save(eq(BPPwC))).thenReturn(BPPwC);

                // act
                MvcResult response = mockMvc.perform(
                                post("/api/ucsbdiningcommonsmenuitem/post?name=BakedPestoPastawithChicken&diningCommonsCode=ortega&station=EntreeSpecials")
                                                .with(csrf()))
                                .andExpect(status().isOk()).andReturn();

                // assert
                verify(ucsbDiningCommonsMenuItemsRepository, times(1)).save(BPPwC);
                String expectedJson = mapper.writeValueAsString(BPPwC);
                String responseString = response.getResponse().getContentAsString();
                assertEquals(expectedJson, responseString);
        }    

        @WithMockUser(roles = { "ADMIN", "USER" })
        @Test
        public void admin_can_delete_an_item() throws Exception {
                // arrange

                UCSBDiningCommonsMenuItem BPPwC = UCSBDiningCommonsMenuItem.builder()
                                .name("Baked Pesto Pasta with Chicken")
                                .diningCommonsCode("ortega")
                                .station("Entree Specials")
                                .build();

                when(ucsbDiningCommonsMenuItemsRepository.findById(eq(1L))).thenReturn(Optional.of(BPPwC));

                // act
                MvcResult response = mockMvc.perform(
                                delete("/api/ucsbdiningcommonsmenuitem?id=1")
                                                .with(csrf()))
                                .andExpect(status().isOk()).andReturn();

                // assert
                verify(ucsbDiningCommonsMenuItemsRepository, times(1)).findById(1L);
                verify(ucsbDiningCommonsMenuItemsRepository, times(1)).delete(any());

                Map<String, Object> json = responseToJson(response);
                assertEquals("UCSBDiningCommonsMenuItem with id 1 deleted", json.get("message"));
        }

        @WithMockUser(roles = { "ADMIN", "USER" })
        @Test
        public void admin_tries_to_delete_non_existant_item_and_gets_right_error_message()
                        throws Exception {
                // arrange

                when(ucsbDiningCommonsMenuItemsRepository.findById(eq(114514L))).thenReturn(Optional.empty());

                // act
                MvcResult response = mockMvc.perform(
                                delete("/api/ucsbdiningcommonsmenuitem?id=114514")
                                                .with(csrf()))
                                .andExpect(status().isNotFound()).andReturn();

                // assert
                verify(ucsbDiningCommonsMenuItemsRepository, times(1)).findById(114514L);
                Map<String, Object> json = responseToJson(response);
                assertEquals("UCSBDiningCommonsMenuItem with id 114514 not found", json.get("message"));
        }

        @WithMockUser(roles = { "ADMIN", "USER" })
        @Test
        public void admin_can_edit_an_existing_item() throws Exception {
                // arrange

                UCSBDiningCommonsMenuItem item_Orig = UCSBDiningCommonsMenuItem.builder()
                                .name("Baked Pesto Pasta with Chicken")
                                .diningCommonsCode("ortega")
                                .station("Entree Specials")
                                .build();

                UCSBDiningCommonsMenuItem item_Edited = UCSBDiningCommonsMenuItem.builder()
                                .name("Cream of Broccoli Soup (v)")
                                .diningCommonsCode("portola")
                                .station("Greens & Grains")
                                .build();

                String requestBody = mapper.writeValueAsString(item_Edited);

                when(ucsbDiningCommonsMenuItemsRepository.findById(eq(1L))).thenReturn(Optional.of(item_Orig));

                // act
                MvcResult response = mockMvc.perform(
                                put("/api/ucsbdiningcommonsmenuitem?id=1")
                                                .contentType(MediaType.APPLICATION_JSON)
                                                .characterEncoding("utf-8")
                                                .content(requestBody)
                                                .with(csrf()))
                                .andExpect(status().isOk()).andReturn();

                // assert
                verify(ucsbDiningCommonsMenuItemsRepository, times(1)).findById(1L);
                verify(ucsbDiningCommonsMenuItemsRepository, times(1)).save(item_Edited); // should be saved with updated info
                String responseString = response.getResponse().getContentAsString();
                assertEquals(requestBody, responseString);
        }

        @WithMockUser(roles = { "ADMIN", "USER" })
        @Test
        public void admin_cannot_edit_item_that_does_not_exist() throws Exception {
                // arrange
                UCSBDiningCommonsMenuItem BPPwC_Edited = UCSBDiningCommonsMenuItem.builder()
                                .name("Baked Pesto Pasta with Chicken Breast")
                                .diningCommonsCode("ortega")
                                .station("Entree Specials")
                                .build();

                String requestBody = mapper.writeValueAsString(BPPwC_Edited);

                when(ucsbDiningCommonsMenuItemsRepository.findById(eq(1L))).thenReturn(Optional.empty());

                // act
                MvcResult response = mockMvc.perform(
                                put("/api/ucsbdiningcommonsmenuitem?id=1")
                                                .contentType(MediaType.APPLICATION_JSON)
                                                .characterEncoding("utf-8")
                                                .content(requestBody)
                                                .with(csrf()))
                                .andExpect(status().isNotFound()).andReturn();

                // assert
                verify(ucsbDiningCommonsMenuItemsRepository, times(1)).findById(1L);
                Map<String, Object> json = responseToJson(response);
                assertEquals("UCSBDiningCommonsMenuItem with id 1 not found", json.get("message"));

        }
}

