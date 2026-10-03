interface HeaderProps {
    onLogout: () => void;
}

function Header({ onLogout }: HeaderProps) {

    return (

        <header className="header">

            <div className="header-content">

                <div>

                    <h2>
                        Codebase Intelligence
                    </h2>

                    <p>
                        Understand your software architecture
                    </p>

                </div>

                <div className="header-right">

                    <div className="backend-status">

                        <span className="status-dot"></span>

                        Backend Connected

                    </div>

                    <button
                        className="logout-button"
                        onClick={onLogout}
                    >
                        Logout
                    </button>

                </div>

            </div>

        </header>

    );

}

export default Header;