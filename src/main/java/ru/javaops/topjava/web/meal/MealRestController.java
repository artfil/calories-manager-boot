package ru.javaops.topjava.web.meal;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.lang.Nullable;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;
import ru.javaops.topjava.model.Meal;
import ru.javaops.topjava.repository.MealRepository;
import ru.javaops.topjava.repository.UserRepository;
import ru.javaops.topjava.to.MealTo;
import ru.javaops.topjava.util.MealsUtil;
import ru.javaops.topjava.util.ValidationUtil;
import ru.javaops.topjava.web.AuthUser;

import javax.validation.Valid;
import java.net.URI;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

import static ru.javaops.topjava.util.DateTimeUtil.atStartOfDayOrMin;
import static ru.javaops.topjava.util.DateTimeUtil.atStartOfNextDayOrMax;
import static ru.javaops.topjava.util.ValidationUtil.*;

@RestController
@RequestMapping(value = MealRestController.REST_URL, produces = MediaType.APPLICATION_JSON_VALUE)
@Slf4j
@AllArgsConstructor
public class MealRestController {
    static final String REST_URL = "/rest/profile/meals";

    private final MealRepository mealRepository;
    private final UserRepository userRepository;

    @GetMapping("/{id}")
    public ResponseEntity<Meal> get(@AuthenticationPrincipal AuthUser authUser, @PathVariable int id) {
        log.info("get meal {} for user {}", id, authUser.id());
        return ResponseEntity.of(mealRepository.get(id, authUser.id()));
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@AuthenticationPrincipal AuthUser authUser, @PathVariable int id) {
        log.info("delete {} for user {}", id, authUser.id());
        checkSingleModification(mealRepository.delete(id, authUser.id()), "Meal id=" + id + ", user id=" + authUser.id() + " missed");
    }

    @GetMapping
    public List<MealTo> getAll(@AuthenticationPrincipal AuthUser authUser) {
        log.info("getAll for user {}", authUser.id());
        return MealsUtil.getTos(mealRepository.getAll(authUser.id()), authUser.getUser().getCaloriesPerDay());
    }


    @PutMapping(value = "/{id}", consumes = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void update(@AuthenticationPrincipal AuthUser authUser, @Valid @RequestBody Meal meal, @PathVariable int id) {
        int userId = authUser.id();
        log.info("update {} for user {}", meal, userId);
        assureIdConsistent(meal, id);
        ValidationUtil.checkNotFoundWithId(mealRepository.get(id, authUser.id()), "Meal id=" + id + " doesn't belong to user id=" + userId);
        meal.setUser(userRepository.getOne(userId));
        mealRepository.save(meal);
    }

    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Meal> createWithLocation(@AuthenticationPrincipal AuthUser authUser, @Valid @RequestBody Meal meal) {
        int userId = authUser.id();
        log.info("create {} for user {}", meal, userId);
        checkNew(meal);
        meal.setUser(userRepository.getOne(userId));
        Meal created = mealRepository.save(meal);
        URI uriOfNewResource = ServletUriComponentsBuilder.fromCurrentContextPath()
                .path(REST_URL + "/{id}")
                .buildAndExpand(created.getId()).toUri();
        return ResponseEntity.created(uriOfNewResource).body(created);
    }


    @GetMapping("/filter")
    public List<MealTo> getBetween(@AuthenticationPrincipal AuthUser authUser,
                                   @RequestParam @Nullable @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
                                   @RequestParam @Nullable @DateTimeFormat(iso = DateTimeFormat.ISO.TIME) LocalTime startTime,
                                   @RequestParam @Nullable @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
                                   @RequestParam @Nullable @DateTimeFormat(iso = DateTimeFormat.ISO.TIME) LocalTime endTime) {

        int userId = authUser.id();
        log.info("getBetween dates({} - {}) time({} - {}) for user {}", startDate, endDate, startTime, endTime, userId);
        List<Meal> mealsDateFiltered = mealRepository.getBetweenHalfOpen(atStartOfDayOrMin(startDate), atStartOfNextDayOrMax(endDate), userId);
        return MealsUtil.getFilteredTos(mealsDateFiltered, authUser.getUser().getCaloriesPerDay(), startTime, endTime);
    }
}