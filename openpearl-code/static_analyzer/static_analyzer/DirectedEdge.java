package static_analyzer;

public class DirectedEdge
{
    public String a;
    public String b;

    public DirectedEdge(String a, String b)
    {
        this.a = a;
        this.b = b;
    }

    public boolean equals(DirectedEdge other)
    {
        return ((a.equals(other.a)) && (b.equals(other.b)));
    }

    public boolean isOpposite(DirectedEdge other)
    {
        return ((a.equals(other.b)) && (b.equals(other.a)));
    }
}
