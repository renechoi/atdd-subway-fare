package nextstep.service;

import nextstep.auth.AuthenticationException;
import nextstep.domain.subway.FavoritePath;
import nextstep.domain.subway.Station;
import nextstep.domain.member.Member;
import nextstep.domain.member.MemberRepository;
import nextstep.domain.subway.PathType;
import nextstep.dto.FavoritePathRequest;
import nextstep.dto.FavoritePathResponse;
import nextstep.repository.FavoritePathRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityNotFoundException;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
public class FavoritePathService {
    private final FavoritePathRepository favoritePathRepository;
    private final PathService pathService;
    private final MemberRepository memberRepository;
    private final StationService stationService;

    public FavoritePathService(FavoritePathRepository favoritePathRepository, PathService pathService,StationService stationService, MemberRepository memberRepository){
        this.favoritePathRepository = favoritePathRepository;
        this.pathService = pathService;
        this.stationService = stationService;
        this.memberRepository = memberRepository;
    }

    @Transactional
    public FavoritePath createFavoritePath(FavoritePathRequest favoritePathRequest,Member member){

        Station sourceStation = stationService.findStation(favoritePathRequest.getSource());
        Station targetStation = stationService.findStation(favoritePathRequest.getTarget());

        pathService.validatePath(sourceStation.getId(), targetStation.getId(), PathType.DISTANCE);

        FavoritePath favoritePath = favoritePathRepository.save(new FavoritePath(sourceStation, targetStation, member));

        return favoritePath;
    }

    public List<FavoritePathResponse> findAllFavoritePaths(Member member){

        List<FavoritePathResponse> favoritePathResponseList = favoritePathRepository.findByMember(member).stream()
                .map(FavoritePathResponse::createFavoritePathResponse)
                .collect(Collectors.toList());

        return favoritePathResponseList;
    }

    @Transactional
    public void deleteFavoritePath(Long favoritePathId,Member member){
        FavoritePath favoritePath = favoritePathRepository.findById(favoritePathId)
                .orElseThrow(() -> new EntityNotFoundException("favoritePath not found"));


        if(!Objects.equals(favoritePath.getMember().getId(), member.getId())){
            throw new AuthenticationException();
        }

        favoritePathRepository.delete(favoritePath);

    }

}