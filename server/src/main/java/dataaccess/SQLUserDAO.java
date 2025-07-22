package dataaccess;

import model.UserData;
import org.eclipse.jetty.server.Authentication;
import org.mindrot.jbcrypt.BCrypt;

public class SQLUserDAO extends SQLParent implements UserDAO {

    public SQLUserDAO() {
    }


    @Override
    public UserData getUser(String username) throws DataAccessException {

        try (var conn = DatabaseManager.getConnection()) {
            var statement = "SELECT password, email FROM `userData` WHERE username=?";
            try (var ps = conn.prepareStatement(statement)) {
                ps.setNString(1, username);
                try (var rs = ps.executeQuery()) {
                    if (rs.next()) {
                        String password = rs.getString("password");
                        String email = rs.getString("email");
                        return new UserData(username, password, email);
                    } else {
                        throw new DataAccessException("User not found");
                    }
                }
            }
        } catch (Exception e) {
            throw new DataAccessException("No User By This Username due to error: " + e);
        }
    }

    @Override
    public void createUser(UserData user) throws DataAccessException {

        var statement = "INSERT INTO userData (username, password, email) VALUES (?, ?, ?)";
        String username = user.username();
        String password = BCrypt.hashpw(user.password(), BCrypt.gensalt());
        String email = user.email();

        if (executeUpdate(statement, username, password, email) > 0) {
            System.out.println("Passed Create User");
        } else {
            System.out.println("Didn't pass createUser");
        }


    }

    @Override
    public void clear() throws DataAccessException {
        var statement = "DELETE FROM userData";
        try {
            executeUpdate(statement);
        } catch (DataAccessException e) {
            throw new DataAccessException("Error clearing userData table: " + e);
        }
    }

}
