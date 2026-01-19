const functions = require("firebase-functions");
const cors = require("cors")({ origin: true });
const OpenAI = require("openai");

const openai = new OpenAI({
  apiKey: process.env.OPENAI_API_KEY,
});

exports.detectCategory = functions
  .runWith({ secrets: ["OPENAI_API_KEY"] })
  .https.onRequest((req, res) => {
    cors(req, res, async () => {
      try {
        const text = (req.body.text || "").trim();
        if (!text) {
          return res.status(400).json({ error: "Missing text" });
        }

        const categories = [
          { key: "dairy", label: "מוצרי חלב" },
          { key: "vegetables", label: "ירקות" },
          { key: "fruits", label: "פירות" },
          { key: "meat", label: "בשר" },
          { key: "fish", label: "דגים" },
          { key: "dry", label: "יבשים" },
          { key: "snacks", label: "חטיפים" },
          { key: "cleaning", label: "ניקיון" },
          { key: "pharmacy", label: "פארם" },
          { key: "bakery", label: "מאפיה" },
          { key: "frozen", label: "קפואים" },
          { key: "other", label: "אחר" },
        ];

        const prompt = `
You classify grocery products into ONE category.
The product name may be in Hebrew or English.
Return ONLY a JSON object with "key" and "label" exactly as in the list.

Categories:
${categories.map(c => `- ${c.key} (${c.label})`).join("\n")}

Product: "${text}"
        `.trim();

        const completion = await openai.chat.completions.create({
          model: "gpt-4o-mini",
          messages: [{ role: "user", content: prompt }],
          temperature: 0,
        });

        const content = completion.choices[0].message.content;
        const result = JSON.parse(content);

        return res.json(result);
      } catch (err) {
        console.error(err);
        return res.status(500).json({ error: "Failed to detect category" });
      }
    });
  });
