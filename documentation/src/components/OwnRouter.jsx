import { BrowserRouter, Route, Routes } from "react-router";
import App from "../App";

const OwnRouter = () => {
  return (
    <BrowserRouter>
      <Routes>
        <Route path="/" element={<App />} />
        <Route path="/about" element={<div>About Page</div>} />
      </Routes>
    </BrowserRouter>
  );
};

export default OwnRouter;
