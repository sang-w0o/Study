<h1>React Helmet</h1>

* `react-helmet` 모듈은 웹 페이지의 head를 수정하기 쉽게 해준다.
* `yarn add react-helmet`

* 예시
```js
import Helmet from 'react-helmet';

const HomePresenter = ({ nowPlaying, upcoming, popular, loading, error }) => (loading ? <Loader /> :
    <Container>
        <Helmet>
            <title>Movies | Nomflix</title>
        </Helmet>
        {nowPlaying && nowPlaying.length > 0 && <Section title="Now Playing">{nowPlaying.map(movie => <Poster key={movie.id} id={movie.id} title={movie.original_title} imageUrl={movie.poster_path}
            rating={movie.vote_average} isMovie={true} year={movie.release_date && movie.release_date.substring(0, 4)} />)}</Section>}
        {upcoming && upcoming.length > 0 && <Section title="Upcoming Movies">{upcoming.map(movie => <Poster key={movie.id} id={movie.id} title={movie.original_title} imageUrl={movie.poster_path}
            rating={movie.vote_average} isMovie={true} year={movie.release_date && movie.release_date.substring(0, 4)} />)}</Section>}
        {popular && popular.length > 0 && <Section title="Popular Movies">{popular.map(movie => <Poster key={movie.id} id={movie.id} title={movie.original_title} imageUrl={movie.poster_path}
            rating={movie.vote_average} isMovie={true} year={movie.release_date && movie.release_date.substring(0, 4)} />)}</Section>}
        {error && <Message color="#e74c3c" text={error} />}
    </Container>
);
```