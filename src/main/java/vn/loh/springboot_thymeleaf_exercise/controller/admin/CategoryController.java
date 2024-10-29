package vn.loh.springboot_thymeleaf_exercise.controller.admin;

import jakarta.validation.Valid;
import org.apache.coyote.BadRequestException;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.ui.ModelMap;
import org.springframework.util.StringUtils;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.ModelAndView;
import vn.loh.springboot_thymeleaf_exercise.dtos.CategoryDTO;
import vn.loh.springboot_thymeleaf_exercise.entities.Category;
import vn.loh.springboot_thymeleaf_exercise.service.ICategoryService;

import java.awt.print.Pageable;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDate;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@Controller
@RequestMapping("/admin/categories")
public class CategoryController {
    @Autowired
    ICategoryService categoryService;

    @GetMapping("")
    public String showCategories(Model model) {
        List<Category> categories = categoryService.findAll();
        model.addAttribute("categories", categories);
        return "admin/categories/list-category";
    }

    @GetMapping("/add")
    public String showAddCategoryForm(Model model) {
        CategoryDTO categoryDTO = new CategoryDTO();
        categoryDTO.setIsEdit(true);
        model.addAttribute("category", categoryDTO);
        return "admin/categories/add-category";
    }

    @PostMapping("/insert")
    public ModelAndView insertCategory(ModelMap model,
                                       @Valid @ModelAttribute("category") CategoryDTO categoryDTO,
                                       BindingResult result,
                                       @RequestParam(value = "file") MultipartFile file) throws IOException {
        if (result.hasErrors()) {
            return new ModelAndView("admin/categories/add-category");
        }
        Category category = new Category();
        String thumbnail = "";
        if (file == null) {
            thumbnail = "";
        } else {
            if (!isValidSuffixImage(Objects.requireNonNull(file.getOriginalFilename()))) {
                throw new BadRequestException("Image is not valid");
            }
            thumbnail = storeFile(file);
        }
        categoryDTO.setImages(thumbnail);
        BeanUtils.copyProperties(categoryDTO, category);
        categoryService.save(category);
        String message = "Category added successfully";
        model.addAttribute("message", message);
        return new ModelAndView("redirect:/admin/categories", model);
    }

    private boolean isValidSuffixImage(String img) {
        return img.endsWith(".jpg") || img.endsWith(".jpeg") ||
                img.endsWith(".png") || img.endsWith(".gif") ||
                img.endsWith(".bmp");
    }

    private boolean isImage(MultipartFile file) {
        String contentType = file.getContentType();
        return contentType != null && contentType.startsWith("image/");
    }

    private String storeFile(MultipartFile file) throws IOException {
        if (file.getSize() == 0)
            return "Anh bi rong";
        if (file.getSize() > 10 * 1024 * 1024) {
            return "File is too large. Maximum size is 10MB";
        }
        if (!isImage(file)) {
            return "File is not an image";
        }
        // get file name
        String fileName = file.getOriginalFilename();
        // generate code random base on UUID
        String uniqueFileName = UUID.randomUUID().toString() + "_" + LocalDate.now() + "_" + fileName;
        java.nio.file.Path uploadDir = Paths.get("uploads");
        // check and create if haven't existed
        if (!Files.exists(uploadDir)) {
            Files.createDirectory(uploadDir);
        }
        java.nio.file.Path destination = Paths.get(uploadDir.toString(), uniqueFileName);
        Files.copy(file.getInputStream(), destination, StandardCopyOption.REPLACE_EXISTING);

        return uniqueFileName;
    }

    @PostMapping("/edit")
    public ModelAndView editCategory(ModelMap model,
                                     @ModelAttribute("category") CategoryDTO categoryDTO) {
        Category category = new Category();
        BeanUtils.copyProperties(categoryDTO, category);
        categoryService.save(category);
        String message = "Category updated successfully";
        model.addAttribute("message", message);
        return new ModelAndView("redirect:/admin/categories", model);
    }

    @GetMapping("/edit")
    public ModelAndView edit(ModelMap modelMap,
                             @RequestParam("id") Long categoryId) {
        Optional<Category> optionalCategory = categoryService.findById(categoryId);
        CategoryDTO categoryDTO = new CategoryDTO();
        if (optionalCategory.isPresent()) {
            Category category = optionalCategory.get();
            BeanUtils.copyProperties(category, categoryDTO);
            categoryDTO.setIsEdit(true);
            modelMap.addAttribute("category", categoryDTO);
            return new ModelAndView("admin/categories/edit-category", modelMap);
        }
        modelMap.addAttribute("message", "Category is not existed");
        return new ModelAndView("redirect:/admin/categories", modelMap);
    }

    @GetMapping("/delete")
    public ModelAndView delete(ModelMap modelMap,
                               @RequestParam("id") Long categoryId) {
        categoryService.deleteById(categoryId);
        modelMap.addAttribute("message", "Category is deleted");
        return new ModelAndView("redirect:/admin/categories", modelMap);
    }

    @GetMapping("/search")
    public String search(ModelMap modelMap,
                         @RequestParam(name = "name", required = false) String name) {
        List<Category> categories = null;
        if (StringUtils.hasText(name)) {
            categories = categoryService.findByName(name);
        } else {
            categories = categoryService.findAll();
        }
        modelMap.addAttribute("categories", categories);
        return "admin/categories";
    }

    @RequestMapping("/searchpaginated")
    public String search(ModelMap modelMap,
                         @RequestParam(name = "name", required = false) String name,
                         @RequestParam(name = "page") Optional<Integer> page,
                         @RequestParam(name = "size") Optional<Integer> size) {
        int count = (int) categoryService.count();
        int currentPage = page.orElse(1);
        int pageSize = size.orElse(3);
        Pageable pageable = (Pageable) PageRequest.of(currentPage - 1, pageSize, Sort.by("name"));
        Page<Category> resultPage = null;
        if (StringUtils.hasText(name)) {
            resultPage = categoryService.findByName(name, (org.springframework.data.domain.Pageable) pageable);
            modelMap.addAttribute("name", name);
        } else {
            resultPage = categoryService.findAll((org.springframework.data.domain.Pageable) pageable);
        }
        int totalPage = resultPage.getTotalPages();
        if (totalPage > 0) {
            int start = Math.max(1, currentPage - 2);
            int end = Math.min(currentPage + 2, totalPage);
            if (totalPage > count) {
                if (end == totalPage) {
                    start = end - count;
                } else if (start == 1) {
                    end = start + count;
                }
            }
            List<Integer> pageNumber = IntStream
                    .rangeClosed(start, end)
                    .boxed()
                    .collect(Collectors.toList());
            modelMap.addAttribute("pageNumber", pageNumber);
            modelMap.addAttribute("resultPage", resultPage);
        }
        return "admin/categories/searchpaginated";
    }
}
