/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package modulgame;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import javax.swing.table.DefaultTableModel;

/**
 *
 * @author Fauzan
 */
public class dbConnection {

    public static Connection con;
    public static Statement stm;

    public void connect() {//untuk membuka koneksi ke database
        try {
            String url = "jdbc:mysql://localhost/db_gamepbo";
            String user = "root";
            String pass = "";
            Class.forName("com.mysql.jdbc.Driver");
            con = DriverManager.getConnection(url, user, pass);
            stm = con.createStatement();
            System.out.println("koneksi berhasil;");
        } catch (Exception e) {
            System.err.println("koneksi gagal" + e.getMessage());
        }
    }

    public void updateHighScore(String username, int score, int time) {
        try {
            PreparedStatement pstmt = this.con.prepareStatement("SELECT * FROM highscore WHERE Username=?");
            pstmt.setString(1, username);
            ResultSet rs = pstmt.executeQuery();
            pstmt.clearParameters();
            if (rs.next()) {
                if (rs.getInt("Score_Akhir") < score + time) {
                    pstmt = this.con.prepareStatement("UPDATE highscore SET Score=?, Waktu=?, Score_Akhir=? WHERE Username=?");
                    pstmt.setInt(1, score);
                    pstmt.setInt(2, time);
                    pstmt.setInt(3, time + score);
                    pstmt.setString(4, username);
                    pstmt.executeUpdate();
                    pstmt.clearParameters();
                }
            } else {
                pstmt = this.con.prepareStatement("INSERT INTO highscore (Username, Score, Waktu, Score_Akhir) VALUES (?, ?, ?, ?)");
                pstmt.setString(1, username);
                pstmt.setInt(2, score);
                pstmt.setInt(3, time);
                pstmt.setInt(4, time + score);
                pstmt.executeUpdate();
                pstmt.clearParameters();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public DefaultTableModel readTable() {

        DefaultTableModel dataTabel = null;
        try {
            Object[] column = {"No", "Username", "Score", "Waktu", "Score Akhir"};
            connect();
            dataTabel = new DefaultTableModel(null, column);
            String sql = "Select * from highscore ORDER BY Score Desc";
            ResultSet res = stm.executeQuery(sql);

            int no = 1;
            while (res.next()) {
                Object[] hasil = new Object[5];
                hasil[0] = no;
                hasil[1] = res.getString("Username");
                hasil[2] = res.getString("Score");
                hasil[3] = res.getString("Waktu");
                hasil[4] = res.getString("Score_Akhir");
                no++;
                dataTabel.addRow(hasil);
            }
        } catch (Exception e) {
            System.err.println("Read gagal " + e.getMessage());
        }

        return dataTabel;
    }
}
