package hudson.tasks;

import hudson.Extension;
import hudson.ExtensionList;
import hudson.ExtensionListView;
import hudson.ExtensionPoint;
import hudson.model.User;

import java.util.List;
import java.util.Map;
import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.acegisecurity.Authentication;

public abstract class UserProvider implements ExtensionPoint {
    public abstract @Nullable User resolve(String idOrFullName, boolean create, @Nonnull Map context);

    public abstract @Nullable User resolve(@CheckForNull Authentication a);

    public abstract @Nullable User resolveById(String id, boolean create);

    public abstract @CheckForNull User resolveCurrent();

    public static @Nullable User get(String idOrFullName, boolean create, @Nonnull Map context) {
        for (UserProvider p : all()) {
            User user = p.resolve(idOrFullName, create, context);
            if(user!=null) return user;
        }
    
        return null;
    }

    public static @CheckForNull User get(@CheckForNull Authentication a) {
        for (UserProvider p : all()) {
            User user = p.resolve(a);
            if(user!=null) return user;
        }

        return null;
    }

    public static @Nullable User getById(String id, boolean create) {
        for (UserProvider p : all()) {
            User user = p.resolveById(id, create);
            if(user!=null) return user;
        }

        return null;
    }

    public static @CheckForNull User current() {
        for (UserProvider p : all()) {
            User user = p.resolveCurrent();
            if(user!=null) return user;
        }

        return null;
    }
        
    /**
     * Returns all the registered {@link UserProvider} descriptors.
     */
    public static ExtensionList<UserProvider> all() {
        return ExtensionList.lookup(UserProvider.class);
    }

    /**
     * All registered {@link UserProvider} implementations.
     *
     * @deprecated since 2009-02-24.
     *      Use {@link #all()} for read access, and use {@link Extension} for registration.
     */
    @Deprecated
    public static final List<UserProvider> LIST = ExtensionListView.createList(UserProvider.class);
}
