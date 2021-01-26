package xyz.foobar;

public class DiffRender implements DiffRenderer {
    public String render(Diff<?> diff) throws DiffException {
        StringBuilder out = new StringBuilder();
        for (int i = 0; i < diff.getLastChangesAsListString().size(); i++) {
            out.append(diff.getLastChangesAsListString().get(i)).append("\n");
        }
        return out.toString();
    }
}
