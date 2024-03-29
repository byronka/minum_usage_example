package example1;

import example1.auth.AuthUtils;
import example1.auth.LoopingSessionReviewing;
import example1.auth.SessionId;
import example1.auth.User;
import example1.sampledomain.ListPhotos;
import example1.sampledomain.PersonName;
import example1.sampledomain.SampleDomain;
import example1.sampledomain.UploadPhoto;
import example1.sampledomain.photo.Photograph;
import minum.Context;
import minum.database.Db;
import minum.web.Response;
import minum.web.StartLine;
import minum.web.WebFramework;

/**
 * This class is where all code gets registered to work
 * with our web testing.
 * <br><br>
 * example:
 * <pre>{@code
 *     wf.registerPath(StartLine.Verb.GET, "formentry", sd::formEntry);
 * }</pre>
 */
public class TheRegister {

    private final Context context;
    private final WebFramework webFramework;

    public TheRegister(Context context) {
        this.context = context;
        this.webFramework = context.getFullSystem().getWebFramework();
    }

    public void registerDomains() {
        var auth = buildAuthDomain();
        var up = setupUploadPhotos(auth);
        var lp = setupListPhotos(auth, up);
        var sd = setupSampleDomain(auth);

        // homepage
        webFramework.registerPath(StartLine.Verb.GET, "", r -> Response.redirectTo("index.html"));
        webFramework.registerPath(StartLine.Verb.GET, "index", sd::sampleDomainIndex);

        // sample domain stuff
        webFramework.registerPath(StartLine.Verb.GET, "formentry", sd::formEntry);
        webFramework.registerPath(StartLine.Verb.POST, "testform", sd::testform);
        webFramework.registerPath(StartLine.Verb.GET, "hello", sd::helloName);

        // photos stuff
        webFramework.registerPath(StartLine.Verb.GET, "photos", lp::ListPhotosPage);
        webFramework.registerPath(StartLine.Verb.GET, "upload", up::uploadPage);
        webFramework.registerPath(StartLine.Verb.POST, "upload", up::uploadPageReceivePost);
        webFramework.registerPath(StartLine.Verb.GET, "photo", lp::grabPhoto);

        // minum.auth stuff
        webFramework.registerPath(StartLine.Verb.GET, "login", auth::login);
        webFramework.registerPath(StartLine.Verb.GET, "register", auth::register);
        webFramework.registerPath(StartLine.Verb.POST, "registeruser", auth::registerUser);
        webFramework.registerPath(StartLine.Verb.POST, "loginuser", auth::loginUser);
        webFramework.registerPath(StartLine.Verb.GET, "logout", auth::logout);
        webFramework.registerPath(StartLine.Verb.GET, "auth", auth::authPage);

    }

    private SampleDomain setupSampleDomain(AuthUtils auth) {
        Db<PersonName> sampleDomainDb = webFramework.getDb("names", PersonName.EMPTY);
        return new SampleDomain(sampleDomainDb, auth);
    }

    private ListPhotos setupListPhotos(AuthUtils auth, UploadPhoto up) {
        return new ListPhotos(context, up, auth);
    }

    private UploadPhoto setupUploadPhotos(AuthUtils auth) {
        Db<Photograph> photoDb = webFramework.getDb("photos", Photograph.EMPTY);
        return new UploadPhoto(photoDb, auth, context);
    }

    private AuthUtils buildAuthDomain() {
        Db<SessionId> sessionDb = webFramework.getDb("sessions", SessionId.EMPTY);
        Db<User> userDb = webFramework.getDb("users", User.EMPTY);
        var au = new AuthUtils(sessionDb, userDb, context);
        new LoopingSessionReviewing(context, au).initialize();
        au.setSessionLooper();
        return au;
    }
}