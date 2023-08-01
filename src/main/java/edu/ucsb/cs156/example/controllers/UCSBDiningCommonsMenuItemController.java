package edu.ucsb.cs156.example.controllers;

import edu.ucsb.cs156.example.entities.UCSBDiningCommonsMenuItem;
import edu.ucsb.cs156.example.errors.EntityNotFoundException;
import edu.ucsb.cs156.example.repositories.UCSBDiningCommonsMenuItemsRepository;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;

@Tag(name = "UCSBDiningCommonsMenuItems")
@RequestMapping("/api/ucsbdiningcommonsmenuitem")
@RestController
@Slf4j
public class UCSBDiningCommonsMenuItemController extends ApiController {
    
    @Autowired
    UCSBDiningCommonsMenuItemsRepository ucsbDiningCommonsMenuItemsRepository;
    
    @Operation(summary= "List food items in UCSB Dining Commons")
    @PreAuthorize("hasRole('ROLE_USER')")
    @GetMapping("/all") 
    public Iterable<UCSBDiningCommonsMenuItem> allItemss() {
        Iterable<UCSBDiningCommonsMenuItem> items = ucsbDiningCommonsMenuItemsRepository.findAll();
        return items;
    }

    @Operation(summary= "Get a single item")
    @PreAuthorize("hasRole('ROLE_USER')")
    @GetMapping("")
    public UCSBDiningCommonsMenuItem getById(
            @Parameter(name="id") @RequestParam Long id) {
        UCSBDiningCommonsMenuItem items = ucsbDiningCommonsMenuItemsRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException(UCSBDiningCommonsMenuItem.class, id));

        return items;
    }

    @Operation(summary= "Create a new item")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @PostMapping("/post")
    public UCSBDiningCommonsMenuItem postItems(
        @Parameter(name="diningCommonsCode") @RequestParam String diningCommonsCode,
        @Parameter(name="name") @RequestParam String name,
        @Parameter(name="station") @RequestParam String station
        )
        {

        UCSBDiningCommonsMenuItem item = new UCSBDiningCommonsMenuItem();
        item.setDiningCommonsCode(diningCommonsCode);
        item.setName(name);
        item.setStation(station);

        UCSBDiningCommonsMenuItem savedItem = ucsbDiningCommonsMenuItemsRepository.save(item);

        return savedItem;
    }

    @Operation(summary= "Delete a UCSBDiningCommonsMenuItem")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @DeleteMapping("")
    public Object deleteItem(
            @Parameter(name="id") @RequestParam Long id) {
        UCSBDiningCommonsMenuItem item = ucsbDiningCommonsMenuItemsRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException(UCSBDiningCommonsMenuItem.class, id));

        ucsbDiningCommonsMenuItemsRepository.delete(item);
        return genericMessage("UCSBDiningCommonsMenuItem with id %s deleted".formatted(id));
    }

    @Operation(summary= "Update a single item")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @PutMapping("")
    public UCSBDiningCommonsMenuItem  updateItem(
            @Parameter(name="id") @RequestParam Long id,
            @RequestBody @Valid UCSBDiningCommonsMenuItem  incoming) {

        UCSBDiningCommonsMenuItem item = ucsbDiningCommonsMenuItemsRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException(UCSBDiningCommonsMenuItem.class, id));

        item.setDiningCommonsCode(incoming.getDiningCommonsCode());
        item.setName(incoming.getName());
        item.setStation(incoming.getStation());

        ucsbDiningCommonsMenuItemsRepository.save(item);

        return item;
    }    

}

