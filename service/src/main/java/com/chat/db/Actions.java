package com.chat.db;

import java.math.BigInteger;
import java.sql.Timestamp;
import java.util.*;

import ch.qos.logback.classic.Logger;
import com.chat.DataSources;
import com.chat.db.Tables.*;
import com.chat.tools.Tools;
import org.javalite.activejdbc.Model;
import org.slf4j.LoggerFactory;
import spark.Request;
import spark.Response;

import static com.chat.db.Tables.*;
/**
 * Created by tyler on 6/5/16.
 */
public class Actions {

    public static Logger log = (Logger) LoggerFactory.getLogger(Actions.class);


    public static Comment createComment(Long userId, Long discussionId,
                                       List<Long> parentBreadCrumbs, String text) {

        List<Long> pbs = (parentBreadCrumbs != null) ? new ArrayList<Long>(parentBreadCrumbs) :
                new ArrayList<Long>();


        // find the candidate
        Comment c = Comment.createIt("discussion_id", discussionId,
                "text_", text,
                "user_id", userId);


        Long childId = c.getLong("id");

        // This is necessary, because of the 0 path length to itself one
        pbs.add(childId);

        Collections.reverse(pbs);


        // Create the comment_tree
        for (int i = 0; i < pbs.size(); i++) {

            Long parentId = pbs.get(i);

            // i is the path length
            CommentTree.createIt("parent_id", parentId,
                    "child_id", childId,
                    "path_length", i);
        }

        return c;

    }

    public static Comment editComment(Long commentId, String text) {

        // Find the comment
        Comment c = Comment.findFirst("id = ?", commentId);

        Timestamp cTime = new Timestamp(new Date().getTime());

        // Create with add modified date
        c.set("text_", text, "modified", cTime).saveIt();

        return c;

    }

    public static UserLoginView getOrCreateUserFromCookie(Request req, Response res) {

        String auth = req.cookie("auth");

        UserLoginView uv = null;


        while (uv == null) {

            // if there's an auth, you're good
            if (auth != null) {
                uv = UserLoginView.findFirst("auth = ?" , auth);
                break;
            }

            // Create the user
            User user = createUser();

            // Generate the login
            auth = Tools.generateSecureRandom();
            Login login = Login.createIt("user_id", user.getId(),
                    "auth", auth,
                    "expire_time", Tools.newExpireTimestamp());

            // set the cookies
            Actions.setCookiesForLogin(user, auth, res);

            uv = UserLoginView.findFirst("auth = ?" , auth);
        }


        return uv;
    }

    public static User createUser() {
        User user = User.createIt(
                "name", Tools.generateSecureRandom());
        user.set("name", "user_" + user.getLongId()).saveIt();

        return user;
    }

    public static UserLoginView login(String userOrEmail, String password, Request req, Response res) {

        // Find the user, then create a login for them

        UserView uv = UserView.findFirst("name = ? or email = ?", userOrEmail, userOrEmail);

        Login login;
        if (uv == null) {
            throw new NoSuchElementException("Incorrect user/email");
        } else {
            FullUser fu = FullUser.findFirst("user_id = ?", uv.getLongId());

            String encryptedPassword = fu.getString("password_encrypted");

            Boolean correctPass = Tools.PASS_ENCRYPT.checkPassword(password, encryptedPassword);

            if (correctPass) {

                String auth = Tools.generateSecureRandom();
                login = Login.createIt("user_id", fu.getInteger("user_id"),
                        "auth", auth,
                        "expire_time", Tools.newExpireTimestamp());

                Actions.setCookiesForLogin(fu, auth, res);

            } else {
                throw new NoSuchElementException("Incorrect Password");
            }
        }

        UserLoginView ulv = UserLoginView.findFirst("login_id = ?", login.getLongId());

        return ulv;

    }

    public static UserLoginView signup(String userName, String password, String email, Request req, Response res) {


        // Find the user, then create a login for them

        UserView uv = UserView.findFirst("name = ? or email = ?", userName, email);

        Login login;

        if (uv == null) {

            // Create the user and full user
            User user = User.createIt(
                    "name", userName);

            log.info("encrypting the user password");
            String encryptedPassword = Tools.PASS_ENCRYPT.encryptPassword(password);

            FullUser fu = FullUser.createIt("user_id", user.getId(),
                    "email", email,
                    "password_encrypted", encryptedPassword);

            // now login that user
            String auth = Tools.generateSecureRandom();
            login = Login.createIt("user_id", user.getId(),
                    "auth", auth,
                    "expire_time", Tools.newExpireTimestamp());

            Actions.setCookiesForLogin(fu, auth, res);

        } else {
            throw new NoSuchElementException("Username/email already exists");
        }

        UserLoginView ulv = UserLoginView.findFirst("login_id = ?", login.getLongId());

        return ulv;

    }

    public static String saveCommentVote(Long userId, Long commentId, Integer rank) {

        String message = null;
        // fetch the vote if it exists
        CommentRank c = CommentRank.findFirst("user_id = ? and comment_id = ?",
                userId, commentId);


        if (c == null) {
            if (rank != null) {
                CommentRank.createIt(
                        "comment_id", commentId,
                        "user_id", userId,
                        "rank", rank);
                message = "Comment Vote Created";
            } else {
                message = "Comment Vote not created";
            }
        } else {
            if (rank != null) {
                c.set("rank", rank).saveIt();
                message = "Comment Vote updated";
            }
            // If the rank is null, then delete the ballot
            else {
                c.delete();
                message = "Comment Vote deleted";
            }
        }

        return message;

    }


    public static String setCookiesForLogin(User user, String auth, Response res) {
        Boolean secure = DataSources.SSL;

        res.cookie("auth", auth, DataSources.EXPIRE_SECONDS, secure);
        res.cookie("uid", user.getId().toString(), DataSources.EXPIRE_SECONDS, secure);
        res.cookie("name", user.getString("name"), DataSources.EXPIRE_SECONDS, secure);

        return "Logged in";
    }

    public static String setCookiesForLogin(FullUser fu, String auth, Response res) {
        Boolean secure = DataSources.SSL;

        res.cookie("auth", auth, DataSources.EXPIRE_SECONDS, secure);
        res.cookie("uid", fu.getString("user_id"), DataSources.EXPIRE_SECONDS, secure);
        res.cookie("username", fu.getString("name"), DataSources.EXPIRE_SECONDS, secure);

        return "Logged in";
    }
}
