package ru.ifmo.rain.naumov.student;

import info.kgeorgiy.java.advanced.student.Group;
import info.kgeorgiy.java.advanced.student.Student;
import info.kgeorgiy.java.advanced.student.StudentGroupQuery;
import info.kgeorgiy.java.advanced.student.StudentQuery;

import java.util.*;
import java.util.function.BinaryOperator;
import java.util.function.Function;
import java.util.stream.Collectors;

import static java.util.Collections.sort;
import static java.util.stream.Collectors.groupingBy;
import static java.util.stream.Collectors.toList;

// java -cp . -p . -m info.kgeorgiy.java.advanced.walk StudentQuery ru.ifmo.rain.naumov.student.StudentDB
public class StudentDB implements StudentQuery {
    private<T> List<String> get(List<T> list, Function<? super T, String> mapBy) {
        return list.stream().map(mapBy).collect(toList());
    }

    @Override
    public List<String> getFirstNames(List<Student> students) {
        return get(students, Student::getFirstName);
    }

    @Override
    public List<String> getLastNames(List<Student> students) {
        return get(students, Student::getLastName);
    }

    @Override
    public List<String> getGroups(List<Student> students) {
        return get(students, Student::getGroup);
    }

    @Override
    public List<String> getFullNames(List<Student> students) {
        return get(students, s -> (s.getFirstName() + " " + s.getLastName()));
    }

    @Override
    public Set<String> getDistinctFirstNames(List<Student> students) {
        return getFirstNames(students).stream().collect(Collectors.toCollection(TreeSet::new));
    }

    @Override
    public String getMinStudentFirstName(List<Student> students) {
        return students.isEmpty() ? "" : students.stream().min(Comparator.comparing(Student::getId)).get().getFirstName();
    }


    @Override
    public List<Student> sortStudentsById(Collection<Student> students) {
        return students.stream().sorted(Comparator.comparing(Student::getId)).collect(toList());
    }

    @Override
    public List<Student> sortStudentsByName(Collection<Student> students) {
        return students.stream().sorted(Comparator
                .comparing(Student::getLastName)
                .thenComparing(Student::getFirstName)
                .thenComparing(Student::getId))
                .collect(toList());
    }

    private List<Student> find(Collection<Student> students, Function<? super Student, Boolean> filterBy) {
        return sortStudentsByName(students.stream().filter(filterBy::apply).collect(toList()));
    }

    @Override
    public List<Student> findStudentsByFirstName(Collection<Student> students, String name) {
        return find(students, s -> s.getFirstName().equals(name));
    }

    @Override
    public List<Student> findStudentsByLastName(Collection<Student> students, String name) {
        return find(students, s -> s.getLastName().equals(name));
    }

    @Override
    public List<Student> findStudentsByGroup(Collection<Student> students, String group) {
        return find(students, s -> s.getGroup().equals(group));
    }

    @Override
    public Map<String, String> findStudentNamesByGroup(Collection<Student> students, String group) {
//        return students.stream().
//                filter(s -> s.getGroup().equals(group)).
//                collect(Collectors.groupingBy(Student::getLastName,
//                        TreeMap::new,
//                        Collectors.mapping(Student::getFirstName,
//                                Collectors.minBy(
//                                        Comparator.comparing(Student::getFirstName)
//                                ))));
        return findStudentsByGroup(students, group)
                .stream()
                .collect(Collectors.toMap(
                        Student::getLastName,
                        Student::getFirstName,
                        BinaryOperator.minBy(Comparator.naturalOrder())
                        //(s1, s2) -> s1.compareTo(s2) > 0 ? s2 : s1
                ));
    }
//
//    private Map<String, List<Student>> groupsToStudents(Collection<Student> students) {
//        return students.stream()
//                .collect(Collectors.groupingBy(Student::getGroup,
//                        HashMap::new,
//                        Collectors.mapping(s -> s,
//                                toList())));
//    }
//
//    private List<Group> getSortedGroups(Collection<Student> students, Function<List<Student>, List<Student>> sort) {
//        return groupsToStudents(students).entrySet().stream()
//                .map(g -> new Group(g.getKey(), sort.apply(g.getValue())))
//                .sorted(Comparator.comparing(Group::getName))
//                .collect(toList());
//    }
//
//    @Override
//    public List<Group> getGroupsByName(Collection<Student> students) {
//        return getSortedGroups(students, this::sortStudentsByName);
//    }
//
//    @Override
//    public List<Group> getGroupsById(Collection<Student> students) {
//        return getSortedGroups(students, this::sortStudentsById);
//    }
//
//    public<E> String getLargest(Collection<Student> students, Function<List<Student>, Collection<E>> modify) {
//        return
//                getSortedGroups(students, this::sortStudentsByName).stream()
//                        .max(Comparator.comparing((Group g) -> modify::apply(g.getStudents().size())).(Group::getName, Collections.reverseOrder(String::compareTo))).get().getName())
//                .orElse("");
//    }
//
//    @Override
//    public String getLargestGroup(Collection<Student> students) {
//        return getLargest(students, (x -> x));
//    }
//
//    @Override
//    public String getLargestGroupFirstName(Collection<Student> students) {
//        return getLargest(students, );
//    }
//

}
