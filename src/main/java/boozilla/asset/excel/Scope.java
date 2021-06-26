package boozilla.asset.excel;

public enum Scope {
    SERVER,
    CLIENT,
    SHARE;

    public boolean in(final String requestScope)
    {
        return in(Scope.valueOf(requestScope.toUpperCase()));
    }

    public boolean in(final Scope requestScope)
    {
        return this.equals(SHARE) || requestScope.equals(SHARE) || this.equals(requestScope);
    }

    public boolean not(final String requestScope)
    {
        return not(Scope.valueOf(requestScope.toUpperCase()));
    }

    public boolean not(final Scope requestScope)
    {
        return !this.equals(SHARE) && !requestScope.equals(SHARE) && !this.equals(requestScope);
    }
}
