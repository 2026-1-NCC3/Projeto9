import type { Metadata } from "next";

export const metadata: Metadata = {
  title: "Maya RPG · API",
  description: "Backend da clínica de RPG da Maya Yamamoto",
};

export default function RootLayout({
  children,
}: Readonly<{
  children: React.ReactNode;
}>) {
  return (
    <html lang="pt-BR">
      <body>{children}</body>
    </html>
  );
}
