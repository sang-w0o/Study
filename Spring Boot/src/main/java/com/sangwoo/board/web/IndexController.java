package com.sangwoo.board.web;

import com.sangwoo.board.config.auth.dto.SessionUser;
import com.sangwoo.board.service.posts.PostsService;
import com.sangwoo.board.web.dto.PostsResponseDto;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import javax.servlet.http.HttpSession;

@RequiredArgsConstructor
@Controller
public class IndexController {

    private final PostsService postsService;
    private final HttpSession httpSession;

    @GetMapping("/")
    public String index(Model model) {
        model.addAttribute("posts", postsService.findAllDesc());

        SessionUser user = (SessionUser)httpSession.getAttribute("loginUser");
        if(user != null) {
            System.out.println("user is not null");
            model.addAttribute("loginUserName", user.getName());
        }
        else {
            System.out.println("user is null");
        }
        return "index";
    }

    @GetMapping("/posts/save")
    public String postsSave() {
        return "posts-save";
    }

    @GetMapping("/posts/update/{id}")
    public String postsUpdate(@PathVariable Long id, Model model) {
        PostsResponseDto dto = postsService.findById(id);
        model.addAttribute("post", dto);
        return "posts-update";
    }
}
