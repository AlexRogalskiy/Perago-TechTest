package xyz.foobar;

public class DiffRender implements DiffRenderer {
    public String render(Diff<?> diff) throws DiffException {
        StringBuilder out = new StringBuilder();
        for (int i = 0; i < diff.getLastChanges().size(); i++) {
            out.append(diff.getLastChanges().get(i)).append("\n");
        }
        return out.toString();
    }
}
