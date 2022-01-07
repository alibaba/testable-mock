package com.alibaba.demo.lambda;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * @author jim
 */
@SuppressWarnings("SimplifyStreamApiCallChains")
public class CollectionListCodeDemo implements BaseDemo {
    public void list() {
        List<User> userList = getUsers();
        double d = userList.stream()
                .map(v -> v)
                .map(v -> Function.<User>identity().apply(v))
                .map(User::getId)
                .mapToDouble(Double::valueOf)
                .reduce(0, Double::sum);
                //.reduce(0, (v1, v2) -> v1 + v2);
        blackHole(d);

        blackHole(userList.stream()
                .map(User::getId)
                .mapToLong(Long::valueOf)
                .reduce(0, Long::sum));

        blackHole(userList.stream()
                .map(User::getId)
                .mapToInt(Long::intValue)
                .reduce(0, Integer::sum));

        Map<Long, User> map = userList.stream()
                .filter(User::isActive)
                .collect(Collectors.toMap(User::getId, Function.identity(), (v1, v2) -> v1));
        blackHole(map);

        List<User> l1 = userList.stream()
                .collect(Collectors.toList())
                .stream()
                .collect(Collectors.toSet())
                .stream()
                .collect(Collectors.groupingBy(User::getName))
                .entrySet()
                .stream()
                .filter(v -> v.getValue().stream().anyMatch(User::isActive))
                .map(Map.Entry::getValue)
                .flatMap(Collection::parallelStream)
                .collect(Collectors.groupingBy(User::getId))
                .values()
                .stream()
                .flatMap(List::stream)
                .collect(Collectors.toList());

        blackHole(l1);

        userList.stream()
                .map(User::isActive)
                .reduce(Boolean::logicalAnd)
                .ifPresent(this::blackHole);
    }

    private List<User> getUsers() {
        List<User> userList = new ArrayList<>();
        for (int i=0; i<10; i++) {
            userList.add(getUser(i));
        }
        return userList;
    }

    private User getUser(int index) {
        User u1 = new User();
        u1.id = (long)index;
        u1.name = "u" + index;
        u1.age = index;
        u1.active = true;
        return u1;
    }

    private static class User {
        private Long id;
        private String name;
        private int age;
        private boolean active;

        public Long getId() {
            return id;
        }

        public void setId(Long id) {
            this.id = id;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public int getAge() {
            return age;
        }

        public void setAge(int age) {
            this.age = age;
        }

        public boolean isActive() {
            return active;
        }

        public void setActive(boolean active) {
            this.active = active;
        }
    }
}
