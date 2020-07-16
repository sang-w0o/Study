package com.sangwoo.board.service.posts;

import com.sangwoo.board.domain.posts.Posts;
import com.sangwoo.board.domain.posts.PostsRepository;
import com.sangwoo.board.web.dto.PostsResponseDto;
import com.sangwoo.board.web.dto.PostsSaveRequestDto;
import com.sangwoo.board.web.dto.PostsUpdateRequestDto;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor
@Service
public class PostsService {

    private final PostsRepository postsRepository;

    @Transactional
    public Long save(PostsSaveRequestDto requestDto) {
        return postsRepository.save(requestDto.toEntity()).getId();
    }

    @Transactional
    public Long update(Long id, PostsUpdateRequestDto requestDto) {
        Posts posts = postsRepository.findById(id).orElseThrow(()-> new IllegalArgumentException("해당 게시글이 존재하지 않습니다. id = " + id));
        return id;
    }

    public PostsResponseDto findById(Long id) {
        Posts entity = postsRepository.findById(id).orElseThrow(()->new IllegalArgumentException("해당 게시글이 존재하지 않습니다. id = " + id));
        return new PostsResponseDto(entity);
    }
}
