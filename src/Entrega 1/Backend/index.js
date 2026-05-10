require('dotenv').config();
const express = require('express');
const { createClient } = require('@supabase/supabase-js');
const bcrypt = require('bcryptjs');
const jwt = require('jsonwebtoken');

const app = express();
app.use(express.json());

const supabase = createClient(process.env.SUPABASE_URL, process.env.SUPABASE_KEY);


// --- MIDDLEWARE DE AUTENTICAÇÃO ---
const authenticateToken = (req, res, next) => {
  const authHeader = req.headers['authorization'];
  const token = authHeader && authHeader.split(' ')[1];

  if (!token) return res.status(401).json({ error: 'Acesso negado' });

  jwt.verify(token, process.env.JWT_SECRET, (err, user) => {
    if (err) return res.status(403).json({ error: 'Token inválido' });
    req.user = user;
    next();
  });
};

// --- ROTAS DE AUTH ---

app.get('/', (req, res) => {
  res.send(`
    <h1>🚀 Server ON - App Fisioterapia</h1>
    <p>O backend está rodando com sucesso na porta ${process.env.PORT || 3000}.</p>
    <p>Status: <b>Conectado ao Supabase</b></p>
  `);
});

// Cadastro de Fisioterapeuta
app.post('/auth/register', async (req, res) => {
  const { nome, email, senha } = req.body;
  const hashedPassword = await bcrypt.hash(senha, 10);

  const { data, error } = await supabase
    .from('usuarios')
    .insert([{ nome, email, senha: hashedPassword }])
    .select();

  if (error) return res.status(400).json(error);
  res.status(201).json({ message: 'Usuário criado com sucesso!' });
});

// Login
app.post('/auth/login', async (req, res) => {
  const { email, senha } = req.body;

  const { data: user, error } = await supabase
    .from('usuarios')
    .select('*')
    .eq('email', email)
    .single();

  if (error || !user) return res.status(400).json({ error: 'Usuário não encontrado' });

  const validPassword = await bcrypt.compare(senha, user.senha);
  if (!validPassword) return res.status(400).json({ error: 'Senha incorreta' });

  const token = jwt.sign({ id: user.id, email: user.email }, process.env.JWT_SECRET, { expiresIn: '1d' });
  res.json({ token });
});

// --- ROTAS DA AGENDA (CRUD) ---

// Listar todos os agendamentos do fisioterapeuta logado
app.get('/agenda', authenticateToken, async (req, res) => {
  const { data, error } = await supabase
    .from('agenda')
    .select('*')
    .eq('fisioterapeuta_id', req.user.id);

  if (error) return res.status(400).json(error);
  res.json(data);
});

// Criar novo agendamento
app.post('/agenda', authenticateToken, async (req, res) => {
  const { paciente_nome, atividade, data_hora, observacoes } = req.body;

  const { data, error } = await supabase
    .from('agenda')
    .insert([{ 
        fisioterapeuta_id: req.user.id, 
        paciente_nome, 
        atividade, 
        data_hora, 
        observacoes 
    }]);

  if (error) return res.status(400).json(error);
  res.status(201).json({ message: 'Atividade agendada!' });
});

// Deletar agendamento
app.delete('/agenda/:id', authenticateToken, async (req, res) => {
    const { id } = req.params;
    const { error } = await supabase
      .from('agenda')
      .delete()
      .eq('id', id)
      .eq('fisioterapeuta_id', req.user.id); // Garante que só deleta o que é dele
  
    if (error) return res.status(400).json(error);
    res.json({ message: 'Agendamento removido' });
});

const PORT = process.env.PORT || 3000;
app.listen(PORT, () => console.log(`Servidor rodando na porta ${PORT}`));

// ADICIONE ESTA LINHA:
module.exports = app;