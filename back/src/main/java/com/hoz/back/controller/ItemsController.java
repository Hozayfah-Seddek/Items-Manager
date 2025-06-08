package com.hoz.back.controller;

import com.hoz.back.dto.DateTransfareObject;
import com.hoz.back.exception.ItemNotFoundException;
import com.hoz.back.exception.UserNotFoundException;
import com.hoz.back.model.Categories;
import com.hoz.back.model.Items;
import com.hoz.back.model.Users;
import com.hoz.back.repository.CategoriesRepository;
import com.hoz.back.repository.ItemRepository;
import com.hoz.back.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.time.format.DateTimeParseException;
import java.util.List;

@RestController
@CrossOrigin(origins = "*")
public class ItemsController {

    @Autowired
    private ItemRepository itemRepo;

    @Autowired
    private UserRepository userRepo;

    @Autowired
    private CategoriesRepository categoriesRepo;    private boolean isAdmin(Authentication auth) {
        return auth.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
    }

    private boolean hasAccessToItem(Items item) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String username = auth.getName();
        return isAdmin(auth) || item.getUser().getUsername().equals(username);
    }

    @PostMapping("/items")
    public ResponseEntity<String> createItem(@RequestBody DateTransfareObject request) {
        try {
            LocalDateTime parsedDateTime = LocalDateTime.parse(request.getDateTime());

            Users owner = userRepo.findById(request.getUserId())
                    .orElseThrow(() -> new UserNotFoundException(request.getUserId()));

            // Ensure users can only create items for themselves unless they're admin
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            if (!isAdmin(auth) && !owner.getUsername().equals(auth.getName())) {
                return ResponseEntity.status(403).body("You can only create items for yourself");
            }

            Categories category = categoriesRepo.findById(request.getCategoryId())
                    .orElseThrow(() -> new RuntimeException("Category not found with ID: " + request.getCategoryId()));

            Items item = new Items();
            item.setName(request.getName());
            item.setCategory(category);
            item.setPrice(request.getPrice());
            item.setQuantity(request.getQuantity());
            item.setUnitOfMeasurement(request.getUnitOfMeasurement());
            item.setAmount(item.getQuantity() * request.getPrice());
            item.setDescription(request.getDescription());
            item.setDateTime(parsedDateTime);
            item.setUser(owner);

            itemRepo.save(item);
            return ResponseEntity.ok("Item saved successfully.");
        } catch (DateTimeParseException e) {
            return ResponseEntity.badRequest().body("Invalid datetime format. Must be ISO-8601 (e.g. 2025-05-14T15:45)");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Something went wrong: " + e.getMessage());
        }
    }    @GetMapping("/items")
    public List<Items> getAllItems() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        System.out.println("User authorities: " + auth.getAuthorities());
        System.out.println("Is admin: " + isAdmin(auth));
        
        String username = auth.getName();
        Users user = userRepo.findByUsername(username);
        
        if (isAdmin(auth)) {
            // Admin can see all items
            return itemRepo.findAll();
        } else {
            // Regular users can only see their own items
            return itemRepo.findByUserId(user.getId());
        }
    }

    @GetMapping("/users/{userId}/items")
    public List<Items> getItemsByUser(@PathVariable Long userId) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        System.out.println("DEBUG - Auth details:");
        System.out.println("Username: " + auth.getName());
        System.out.println("Authorities: " + auth.getAuthorities());
        
        String username = auth.getName();
        Users requestingUser = userRepo.findByUsername(username);
        System.out.println("Requesting user ID: " + requestingUser.getId());
        System.out.println("Requested userId param: " + userId);
        
        if (requestingUser.getId().equals(userId) || isAdmin(auth)) {
            return itemRepo.findByUserId(userId);
        }
        
        throw new RuntimeException("You can only view your own items");
    }

    @GetMapping("/item/{itemId}")
    public Items getItemById(@PathVariable Long itemId) {
        Items item = itemRepo.findById(itemId)
                .orElseThrow(() -> new ItemNotFoundException(itemId));
        
        if (!hasAccessToItem(item)) {
            throw new RuntimeException("You don't have access to this item");
        }
        
        return item;
    }

    @PutMapping("/item/{itemId}")
    public Items updateItem(@RequestBody Items newItem, @PathVariable Long itemId) {
        return itemRepo.findById(itemId)
                .map(item -> {
                    if (!hasAccessToItem(item)) {
                        throw new RuntimeException("You don't have access to this item");
                    }
                    
                    item.setName(newItem.getName());
                    item.setCategory(newItem.getCategory());
                    item.setPrice(newItem.getPrice());
                    item.setQuantity(newItem.getQuantity());
                    item.setUnitOfMeasurement(newItem.getUnitOfMeasurement());
                    item.setAmount(newItem.getQuantity() * newItem.getPrice());
                    item.setDescription(newItem.getDescription());
                    item.setDateTime(newItem.getDateTime());
                    return itemRepo.save(item);
                }).orElseThrow(() -> new ItemNotFoundException(itemId));
    }

    @DeleteMapping("/item/{itemId}")
    public String deleteItem(@PathVariable Long itemId) {
        Items item = itemRepo.findById(itemId)
                .orElseThrow(() -> new ItemNotFoundException(itemId));
                
        if (!hasAccessToItem(item)) {
            throw new RuntimeException("You don't have access to this item");
        }
        
        itemRepo.deleteById(itemId);
        return "Item with item-Id " + itemId + " has been deleted successfully.";
    }

    @DeleteMapping("/items/all")
    public String deleteAllItems() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (isAdmin(auth)) {
            itemRepo.deleteAll();
            return "All items have been deleted successfully.";
        } else {
            String username = auth.getName();
            Users user = userRepo.findByUsername(username);
            itemRepo.deleteAll(itemRepo.findByUserId(user.getId()));
            return "All your items have been deleted successfully.";
        }
    }

    @DeleteMapping("/users/{userId}/items/all")
    public String deleteAllUserItems(@PathVariable Long userId) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        Users targetUser = userRepo.findById(userId)
                .orElseThrow(() -> new UserNotFoundException(userId));

        // Only allow admins or the user themselves to delete their items
        if (!isAdmin(auth) && !targetUser.getUsername().equals(auth.getName())) {
            throw new RuntimeException("You can only delete your own items");
        }

        List<Items> userItems = itemRepo.findByUserId(userId);
        itemRepo.deleteAll(userItems);
        return "All items for user " + targetUser.getUsername() + " have been deleted successfully.";
    }
}
