package testcases.T022_factory_method_constructor_dependencies;

@motif.Scope
public interface Scope {

    B b();

    abstract class Objects {
        abstract A a();
        abstract B b();
    }
}
