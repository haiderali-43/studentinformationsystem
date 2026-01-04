import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Objects;
import java.util.Queue;
import java.util.Stack;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
// import com.mongodb.ConnectionString;
// import com.mongodb.MongoClientSettings;
// import com.mongodb.MongoException;
// import com.mongodb.ServerApi;
// import com.mongodb.ServerApiVersion;
// import com.mongodb.client.MongoClient;
// import com.mongodb.client.MongoClients;
// import com.mongodb.client.MongoDatabase;
// import org.bson.Document;

public class StudentInformationManager {

    /* ===================== STUDENT MODEL ===================== */
    static class Student {
        String id;
        String name;
        int age;
        String department;

        Student(String id, String name, int age, String department) {
            this.id = id;
            this.name = name;
            this.age = age;
            this.department = department;
        }

        @Override
        public String toString() {
            return id + " | " + name + " | " + age + " | " + department;
        }
    }

    /* ===================== MONGODB CONNECTION ===================== */
    static class MongoDBConnection {
        // Lightweight in-memory replacement for MongoDB collection
        // Removes the external MongoDB dependency so the project compiles
        static class DBDocument {
            private final Map<String, Object> data = new HashMap<>();

            DBDocument(String key, Object value) {
                data.put(key, value);
            }

            DBDocument append(String key, Object value) {
                data.put(key, value);
                return this;
            }

            Object get(String key) {
                return data.get(key);
            }
        }

        static class SimpleCollection {
            private final java.util.List<DBDocument> storage = new java.util.ArrayList<>();

            void insertOne(DBDocument doc) {
                storage.add(doc);
            }

            void deleteOne(DBDocument query) {
                Object id = query.get("id");
                if (id == null) return;
                storage.removeIf(d -> Objects.equals(d.get("id"), id));
            }
        }

        private static final SimpleCollection COLLECTION = new SimpleCollection();

        static SimpleCollection getCollection() {
            return COLLECTION;
        }
    }

    /* ===================== DSA MANAGER ===================== */
    static class StudentManager {

        // DSA usage
        LinkedList<Student> studentList = new LinkedList<>(); // main storage
        Queue<Student> admissionQueue = new LinkedList<>();   // FIFO
        Stack<Student> undoStack = new Stack<>();              // LIFO

    MongoDBConnection.SimpleCollection collection =
        MongoDBConnection.getCollection();

        void addStudent(Student s) {
            studentList.add(s);          // LinkedList
            admissionQueue.offer(s);     // Queue
            undoStack.push(s);           // Stack

        MongoDBConnection.DBDocument doc = new MongoDBConnection.DBDocument("id", s.id)
            .append("name", s.name)
            .append("age", s.age)
            .append("department", s.department);

        collection.insertOne(doc);
        }

        Student processAdmission() {
            return admissionQueue.poll(); // FIFO
        }

        Student undoLastAdd() {
            if (undoStack.isEmpty()) return null;

            Student s = undoStack.pop();  // LIFO
            studentList.remove(s);
            collection.deleteOne(new MongoDBConnection.DBDocument("id", s.id));
            return s;
        }

        LinkedList<Student> getAllStudents() {
            return studentList;
        }
    }

    /* ===================== GUI ===================== */
    static class StudentGUI {

        StudentManager manager = new StudentManager();

        StudentGUI() {
            JFrame frame = new JFrame("Student Information Manager");
            frame.setSize(650, 450);
            frame.setLayout(new BorderLayout());

            JPanel inputPanel = new JPanel(new GridLayout(5, 2));
            JTextField id = new JTextField();
            JTextField name = new JTextField();
            JTextField age = new JTextField();
            JTextField dept = new JTextField();

            JTextArea output = new JTextArea();
            output.setEditable(false);

            JButton addBtn = new JButton("Add Student");
            JButton viewBtn = new JButton("View All");
            JButton processBtn = new JButton("Process Admission (Queue)");
            JButton undoBtn = new JButton("Undo Last Add (Stack)");

            inputPanel.add(new JLabel("Student ID"));
            inputPanel.add(id);
            inputPanel.add(new JLabel("Name"));
            inputPanel.add(name);
            inputPanel.add(new JLabel("Age"));
            inputPanel.add(age);
            inputPanel.add(new JLabel("Department"));
            inputPanel.add(dept);
            inputPanel.add(addBtn);
            inputPanel.add(viewBtn);

            JPanel bottomPanel = new JPanel();
            bottomPanel.add(processBtn);
            bottomPanel.add(undoBtn);

            frame.add(inputPanel, BorderLayout.NORTH);
            frame.add(new JScrollPane(output), BorderLayout.CENTER);
            frame.add(bottomPanel, BorderLayout.SOUTH);

            addBtn.addActionListener(e -> {
                try {
                    Student s = new Student(
                            id.getText(),
                            name.getText(),
                            Integer.parseInt(age.getText()),
                            dept.getText()
                    );
                    manager.addStudent(s);
                    output.setText("Student Added Successfully\n");
                } catch (Exception ex) {
                    output.setText("Invalid Input\n");
                }
            });

            viewBtn.addActionListener(e -> {
                output.setText("");
                for (Student s : manager.getAllStudents()) {
                    output.append(s + "\n");
                }
            });

            processBtn.addActionListener(e -> {
                Student s = manager.processAdmission();
                output.setText(
                        s == null ? "Admission Queue Empty\n"
                                  : "Processed Admission:\n" + s + "\n"
                );
            });

            undoBtn.addActionListener(e -> {
                Student s = manager.undoLastAdd();
                output.setText(
                        s == null ? "Nothing to Undo\n"
                                  : "Undo Successful:\n" + s + "\n"
                );
            });

            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.setVisible(true);
        }
    }

    /* ===================== MAIN ===================== */
    public static void main(String[] args) {
        new StudentGUI();
    }
}
